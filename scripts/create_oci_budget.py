import sys
import oci
from oci.budget import BudgetClient
from oci.budget.models import CreateBudgetDetails, CreateAlertRuleDetails

def setup_budget():
    print("[*] Loading OCI config from C:/Users/ASUS/.oci/config ...")
    config = oci.config.from_file('C:/Users/ASUS/.oci/config')
    tenancy_id = config['tenancy']
    
    print("[*] Connecting to OCI Budget Service in region:", config.get('region'))
    client = BudgetClient(config)
    
    # 1. Check existing budgets
    print("[*] Checking existing budgets in tenancy...")
    budgets = client.list_budgets(compartment_id=tenancy_id).data
    print(f"[+] Found {len(budgets)} existing budgets.")
    
    target_budget = None
    for b in budgets:
        print(f"    - Budget: '{b.display_name}', ID: {b.id}, Amount: {b.amount}, State: {b.lifecycle_state}")
        if b.display_name == "Zero-Spend-Shield" and b.lifecycle_state == "ACTIVE":
            target_budget = b
            
    if not target_budget:
        print("[*] Creating new $1.00 budget: 'Zero-Spend-Shield' ...")
        create_details = CreateBudgetDetails(
            compartment_id=tenancy_id,
            amount=1.0,
            reset_period="MONTHLY",
            display_name="Zero-Spend-Shield",
            description="Guarantee $0.00 Always Free usage - alert if spend hits $1.00",
            target_type="COMPARTMENT",
            targets=[tenancy_id]
        )
        target_budget = client.create_budget(create_budget_details=create_details).data
        print(f"[SUCCESS] Budget created! ID: {target_budget.id}")
    else:
        print(f"[+] Using existing budget: {target_budget.id}")

    # 2. Check alert rules for this budget
    print(f"[*] Checking alert rules for budget {target_budget.id} ...")
    rules = client.list_alert_rules(budget_id=target_budget.id).data
    print(f"[+] Found {len(rules)} existing alert rules.")
    
    email = "dirzaaulia11@gmail.com"
    has_actual_rule = False
    has_forecast_rule = False
    
    for r in rules:
        print(f"    - Alert: '{r.display_name}', Type: {r.type}, Threshold: {r.threshold}, Recipients: {r.recipients}")
        if r.type == "ACTUAL" and r.lifecycle_state == "ACTIVE":
            has_actual_rule = True
        if r.type == "FORECASTED" and r.lifecycle_state == "ACTIVE":
            has_forecast_rule = True
            
    if not has_actual_rule:
        print("[*] Creating ACTUAL spend alert rule ($1.00 threshold) ...")
        actual_rule = CreateAlertRuleDetails(
            display_name="Actual-Spend-Exceeded-1-Dollar",
            type="ACTUAL",
            threshold=1.0,
            threshold_type="ABSOLUTE",
            recipients=email,
            description="Immediate notification when spend exceeds $1.00"
        )
        res = client.create_alert_rule(budget_id=target_budget.id, create_alert_rule_details=actual_rule).data
        print(f"[SUCCESS] Actual spend alert rule created! ID: {res.id}")
        
    if not has_forecast_rule:
        print("[*] Creating FORECASTED spend alert rule ($1.00 threshold) ...")
        forecast_rule = CreateAlertRuleDetails(
            display_name="Forecasted-Spend-Exceeded-1-Dollar",
            type="FORECAST",
            threshold=1.0,
            threshold_type="ABSOLUTE",
            recipients=email,
            description="Early warning if monthly projected spend exceeds $1.00"
        )
        res = client.create_alert_rule(budget_id=target_budget.id, create_alert_rule_details=forecast_rule).data
        print(f"[SUCCESS] Forecasted spend alert rule created! ID: {res.id}")

    print("\n[ALL DONE] Your $1.00 Oracle Cloud Budget & Emergency Alert Shield is fully ACTIVE!")

if __name__ == "__main__":
    setup_budget()
