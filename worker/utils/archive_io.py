"""Streaming JSON archives with manifests and a bounded restoration check."""

import gzip
import hashlib
import json
from datetime import UTC, datetime
from pathlib import Path


def checksum(path):
    digest = hashlib.sha256()
    with Path(path).open("rb") as stream:
        for chunk in iter(lambda: stream.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def write_archive(path, records, *, season, competition=None):
    path = Path(path)
    path.parent.mkdir(parents=True, exist_ok=True)
    temporary = path.with_suffix(path.suffix + ".partial")
    count = 0
    dates = []
    with gzip.open(temporary, "wt", encoding="utf-8") as stream:
        stream.write("[\n")
        for row in records:
            clean = {
                key: value
                for key, value in row.items()
                if key not in ("youtube_region_info", "backup_youtube_region_info")
            }
            stream.write(("," if count else "") + json.dumps(clean, default=str, ensure_ascii=False) + "\n")
            count += 1
            if row.get("utc_date"):
                dates.append(str(row["utc_date"]))
                if len(dates) > 2:
                    dates = [min(dates), max(dates)]
        stream.write("]\n")
    manifest = {
        "format": "json-array-v1",
        "competition": competition,
        "provider_season": season,
        "records": count,
        "bytes": temporary.stat().st_size,
        "sha256": checksum(temporary),
        "date_from": min(dates) if dates else None,
        "date_to": max(dates) if dates else None,
        "created_at": datetime.now(UTC).isoformat(),
    }
    assert verify_archive(temporary, manifest) == count
    temporary.replace(path)
    path.with_suffix(path.suffix + ".manifest.json").write_text(json.dumps(manifest, indent=2), encoding="utf-8")
    return manifest


def verify_archive(path, manifest):
    if checksum(path) != manifest["sha256"]:
        raise ValueError("Archive checksum mismatch")
    count = 0
    previous = None
    with gzip.open(path, "rt", encoding="utf-8") as stream:
        for line in stream:
            line = line.strip()
            if line in ("[", "]", ""):
                continue
            record = json.loads(line.removeprefix(","))
            identity = int(record["id"])
            if previous is not None and identity <= previous:
                raise ValueError("Archive IDs are not strictly increasing")
            previous = identity
            count += 1
    if count != manifest["records"]:
        raise ValueError("Archive record count mismatch")
    return count
