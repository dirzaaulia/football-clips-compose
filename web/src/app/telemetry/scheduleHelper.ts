export function computeNextCycleSchedule(
  utcDate?: string | null,
  lastCheck?: string | null
): { nextCycleEta: string; nextCycleTimestamp: string } {
  const now = Date.now();
  const matchTime = utcDate ? new Date(utcDate).getTime() : now - 3600 * 1000;
  const lastCheckTime = lastCheck ? new Date(lastCheck).getTime() : matchTime;

  const hoursSinceMatch = Math.max(0, (now - matchTime) / (1000 * 60 * 60));

  let intervalMinutes = 30;
  let tierLabel = 'Tier 1';
  if (hoursSinceMatch > 24) {
    intervalMinutes = 1440;
    tierLabel = 'Tier 4';
  } else if (hoursSinceMatch > 6) {
    intervalMinutes = 360;
    tierLabel = 'Tier 3';
  } else if (hoursSinceMatch > 2) {
    intervalMinutes = 120;
    tierLabel = 'Tier 2';
  } else {
    intervalMinutes = 30;
    tierLabel = 'Tier 1';
  }

  const nextCheckEpoch = lastCheckTime + intervalMinutes * 60 * 1000;
  const diffMinutes = Math.max(1, Math.round((nextCheckEpoch - now) / (60 * 1000)));

  const etaStr =
    diffMinutes <= 1
      ? `Due momentarily (${tierLabel})`
      : `Next check in ~${diffMinutes}m (${tierLabel})`;

  let wibTime = '';
  try {
    const targetDate = new Date(
      nextCheckEpoch > now ? nextCheckEpoch : now + diffMinutes * 60 * 1000
    );
    wibTime =
      'Eligible at ' +
      targetDate.toLocaleTimeString('en-GB', {
        timeZone: 'Asia/Jakarta',
        hour: '2-digit',
        minute: '2-digit',
      }) +
      ' WIB';
  } catch {
    wibTime = '';
  }

  return { nextCycleEta: etaStr, nextCycleTimestamp: wibTime };
}
