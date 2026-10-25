"""Structured cycle telemetry."""


class TelemetryService:
    def _load_history(self):
        return {"cycles": [], "events": [], "pipelines": {}}

    def record_cycle(
        self,
        mode,
        next_sleep_str,
        cycle_index,
        steps_log,
        pipeline_type="FIXTURE_SYNC",
        pipeline_cycle_index=None,
    ):
        return None


telemetry_service = TelemetryService()

