def test_score_and_status_regression_logic():
    # Simulasi data di DB saat ini
    old_data_map = {
        1001: {
            "id": 1001,
            "status": "FINISHED",
            "home_score": 3,
            "away_score": 1
        }
    }

    # Simulasi API mengirimkan data regresi (misal API sempat glitch kirim status SCHEDULED)
    incoming_payload = {
        "id": 1001,
        "status": "SCHEDULED",
        "home_score": None,
        "away_score": None,
        "home_team_name": "Arsenal FC",
        "away_team_name": "Chelsea FC"
    }

    old = old_data_map.get(incoming_payload["id"])
    is_regression = False
    if old["status"] in ["FINISHED", "AWARDED"] and incoming_payload["status"] not in ["FINISHED", "AWARDED"]:
        is_regression = True

    assert is_regression is True

    # Guard action: Pertahankan status dan skor lama
    if is_regression:
        incoming_payload["status"] = old["status"]
        if incoming_payload.get("home_score") is None:
            incoming_payload["home_score"] = old.get("home_score")
        if incoming_payload.get("away_score") is None:
            incoming_payload["away_score"] = old.get("away_score")

    assert incoming_payload["status"] == "FINISHED"
    assert incoming_payload["home_score"] == 3
    assert incoming_payload["away_score"] == 1
