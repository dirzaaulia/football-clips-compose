# Football Clips Worker (v2.0)

Modern, modular, asynchronous data synchronization engine for FootballClips.

## Architecture
- **Config**: Pydantic `BaseSettings` reading environment variables with strict typing.
- **Clients**: Resilient wrappers for Football-Data.org (rate-guarded), YouTube Data API (quota-guarded), Gemini 3.7 Flash AI, and Supabase.
- **Services**:
  - `match_service`: Pulls fixtures, prevents score/status regression, saves to Supabase.
  - `club_service`: Auto-syncs Big 5 league clubs to `supported_clubs` table (zero maintenance across seasons).
  - `highlight_service`: Discovers official club/league video highlights, applies multi-layer title filters, geoblock checks, and Gemini AI re-ranking.
  - `telemetry_service`: Emits structured cycles and event logs to Supabase dashboard.
- **Tests**: Pytest suite for business logic without hitting live external APIs.

## Commands (using `uv`)
```bash
# Install dependencies
uv sync

# Run worker
uv run python main.py

# Run tests
uv run pytest
```
