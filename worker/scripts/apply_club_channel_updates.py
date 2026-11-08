"""
Script to apply resolved YouTube channel IDs and handles to supported_clubs in PostgreSQL.
"""

import json
import subprocess
from loguru import logger

SPECIAL_OVERRIDES = {
    "gimnasia_mendoza": {
        "handle": "@gimnasiayesgrimamendoza",
        "channel_id": "UCiO4QPnKra8XBWubQSzf_QA",
    },
    "rcd_espanyol_de_barcelona": {
        "handle": "@rcdespanyol",
        "channel_id": "UClywhnD01yUU5kO6OgAeHUQ",
    },
    "bodo_glimt": {
        "handle": "@fkbodglimt7528",
        "channel_id": "UCPrcVYq9ieF4rZ4UycQlzsQ",
    },
    "fk_bod_glimt": {
        "handle": "@fkbodglimt7528",
        "channel_id": "UCPrcVYq9ieF4rZ4UycQlzsQ",
    },
}


def main() -> None:
    with open("resolved_channel_updates.json", "r", encoding="utf-8") as f:
        updates = json.load(f)

    sql_statements = []

    for item in updates:
        cid = item["id"]
        handle = item.get("handle") or ""
        ch_id = item.get("channel_id") or ""

        if cid in SPECIAL_OVERRIDES:
            handle = SPECIAL_OVERRIDES[cid]["handle"]
            ch_id = SPECIAL_OVERRIDES[cid]["channel_id"]

        if not ch_id:
            logger.warning(f"Skipping {cid} due to missing channel_id")
            continue

        sql = (
            f"UPDATE supported_clubs "
            f"SET youtube_handle = '{handle}', youtube_channel_id = '{ch_id}' "
            f"WHERE id = '{cid}';"
        )
        sql_statements.append(sql)

    full_sql = "\n".join(sql_statements)
    logger.info(f"Generated {len(sql_statements)} SQL update statements.")

    # Write to temp file for execution
    with open("apply_updates.sql", "w", encoding="utf-8") as f:
        f.write(full_sql)

    # Execute on remote jarvis-postgres via SSH
    host = os.getenv("ORACLE_VM_HOST", "footballclips-oracle")
    remote_cmd = (
        f'ssh -o StrictHostKeyChecking=no ubuntu@{host} '
        '"docker exec -i jarvis-postgres psql -U postgres -d footballclips" < apply_updates.sql'
    )
    logger.info("Executing SQL statements on remote PostgreSQL...")
    res = subprocess.run(remote_cmd, shell=True, capture_output=True, text=True)
    if res.returncode != 0:
        logger.error(f"SQL execution failed: {res.stderr}")
    else:
        logger.info(f"SQL execution succeeded!\n{res.stdout}")


if __name__ == "__main__":
    main()
