from worker.clients.espn_parser import parse_match_summary


def test_parse_match_summary_extracts_pitch_plays():
    mock_payload = {
        "header": {
            "competitions": [
                {
                    "competitors": [
                        {"homeAway": "home", "team": {"id": "1", "displayName": "Arsenal"}},
                        {"homeAway": "away", "team": {"id": "2", "displayName": "Chelsea"}},
                    ],
                },
            ],
        },
        "commentary": [
            {
                "sequence": "100",
                "time": {"displayValue": "79'"},
                "text": "Goal! Noah Okafor right footed shot from the centre of the box.",
                "play": {
                    "id": "54932799",
                    "type": {"id": "70", "text": "Goal", "type": "goal"},
                    "period": {"number": 2},
                    "clock": {"value": 4740.0, "displayValue": "79'"},
                    "team": {"displayName": "Arsenal"},
                    "participants": [{"athlete": {"displayName": "Noah Okafor"}}],
                    "fieldPositionX": 89.7,
                    "fieldPositionY": 55.1,
                },
            },
            {
                "sequence": "101",
                "time": {"displayValue": "82'"},
                "text": "Corner awarded to Chelsea.",
                "play": {
                    "id": "54932805",
                    "type": {"id": "55", "text": "Corner Awarded", "type": "corner"},
                    "period": {"number": 2},
                    "clock": {"value": 4920.0, "displayValue": "82'"},
                    "team": {"displayName": "Chelsea"},
                    "fieldPositionX": 95.0,
                    "fieldPositionY": 5.0,
                },
            },
        ],
    }

    result = parse_match_summary(mock_payload)
    assert result is not None
    assert "pitch_plays" in result
    plays = result["pitch_plays"]
    assert len(plays) == 2

    goal_play = plays[0]
    assert goal_play["clock"] == "79'"
    assert goal_play["type_text"] == "Goal"
    assert goal_play["x_percent"] == 89.7
    assert goal_play["y_percent"] == 55.1
    assert goal_play["player"] == "Noah Okafor"
    assert goal_play["team_name"] == "Arsenal"

    corner_play = plays[1]
    assert corner_play["clock"] == "82'"
    assert corner_play["x_percent"] == 95.0
    assert corner_play["y_percent"] == 5.0
