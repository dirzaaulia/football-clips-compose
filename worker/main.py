"""Main entry point for Football Clips Worker Engine."""

import asyncio
import socket
import sys
from pathlib import Path

# Ensure line-buffered stdout for real-time telemetry logging
if hasattr(sys.stdout, "reconfigure"):
    try:
        sys.stdout.reconfigure(line_buffering=True)
        sys.stderr.reconfigure(line_buffering=True)
    except Exception:
        pass

# Default 20-second socket timeout to prevent any HTTP client from blocking indefinitely
socket.setdefaulttimeout(20.0)

# Ensure root workspace is in sys.path when invoked directly
_pkg_root = str(Path(__file__).resolve().parent.parent)
if _pkg_root not in sys.path:
    sys.path.insert(0, _pkg_root)

from worker.orchestrator import WorkerOrchestrator


def main():
    orchestrator = WorkerOrchestrator()
    asyncio.run(orchestrator.start())


if __name__ == "__main__":
    main()
