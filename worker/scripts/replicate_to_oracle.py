import time
import httpx
from loguru import logger
from worker.clients.supabase_client import supabase_client

ORACLE_POSTGREST_URL = "https://168.110.196.30.sslip.io"

def replicate_matches():
    logger.info("Starting replication of season 2025 matches from Supabase Cloud to Oracle VM Super-Node...")
    
    batch_size = 200
    offset = 0
    total_replicated = 0
    
    headers = {
        "Prefer": "resolution=merge-duplicates",
        "Content-Type": "application/json"
    }

    client = httpx.Client(verify=False, timeout=60.0)

    while True:
        res = (
            supabase_client.table("matches")
            .select("*")
            .eq("season", 2025)
            .order("utc_date", desc=True)
            .range(offset, offset + batch_size - 1)
            .execute()
        )
        rows = res.data or []
        if not rows:
            break

        # PostgREST batch upsert
        r = client.post(
            f"{ORACLE_POSTGREST_URL}/matches",
            json=rows,
            headers=headers
        )

        if r.status_code in [200, 201, 204]:
            total_replicated += len(rows)
            logger.info(f"Replicated batch {offset}..{offset + len(rows)} ({total_replicated} total matches).")
        else:
            logger.error(f"Failed to upsert batch at offset {offset}: status={r.status_code}, text={r.text[:200]}")
            # Try row by row for error resilience if batch has an issue
            for row in rows:
                r_single = client.post(
                    f"{ORACLE_POSTGREST_URL}/matches",
                    json=[row],
                    headers=headers
                )
                if r_single.status_code in [200, 201, 204]:
                    total_replicated += 1
                else:
                    logger.warning(f"Error on match {row.get('id')}: {r_single.text[:150]}")

        offset += batch_size
        time.sleep(0.1)

    logger.info(f"Replication complete! Total replicated to Oracle VM: {total_replicated}")

    # Verify counts
    r_check = client.get(
        f"{ORACLE_POSTGREST_URL}/matches?season=eq.2025&select=id",
        headers={"Prefer": "count=exact"}
    )
    logger.info(f"Verified Oracle VM season 2025 matches count: {r_check.headers.get('content-range')}")

if __name__ == "__main__":
    replicate_matches()
