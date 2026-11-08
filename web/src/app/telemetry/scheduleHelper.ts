export function computeNextCycleSchedule(
  utcDate?: string | null,
  lastCheck?: string | null,
): { nextCycleEta: string; nextCycleTimestamp: string } {
  const now = Date.now();
  const matchTime = utcDate ? new Date(utcDate).getTime() : now - 3600 * 1000;
  const hasLastCheck = Boolean(lastCheck);
  const lastCheckTime = lastCheck ? new Date(lastCheck).getTime() : matchTime;

  const hoursSinceMatch = Math.max(0, (now - matchTime) / (1000 * 60 * 60));

  let intervalMinutes = 30;
  let tierLabel = 'Tier 1';
  if (hoursSinceMatch >= 72) {
    intervalMinutes = 1440;
    tierLabel = 'Tier 4';
  } else if (hoursSinceMatch >= 24) {
    intervalMinutes = 360;
    tierLabel = 'Tier 3';
  } else if (hoursSinceMatch >= 6) {
    intervalMinutes = 120;
    tierLabel = 'Tier 2';
  } else {
    intervalMinutes = 30;
    tierLabel = 'Tier 1';
  }

  const nextCheckEpoch = hasLastCheck ? lastCheckTime + intervalMinutes * 60 * 1000 : matchTime;
  const rawDiffMinutes = Math.round((nextCheckEpoch - now) / (60 * 1000));

  let wibClock = '';
  try {
    const targetDate = new Date(nextCheckEpoch);
    wibClock = targetDate.toLocaleTimeString('en-GB', {
      timeZone: 'Asia/Jakarta',
      hour: '2-digit',
      minute: '2-digit',
    });
  } catch {
    wibClock = '';
  }

  const isDueNow = !hasLastCheck || rawDiffMinutes <= 0;
  const diffMinutes = Math.max(1, rawDiffMinutes);

  const etaStr = isDueNow
    ? wibClock
      ? `Due now • Eligible since ${wibClock} WIB (${tierLabel})`
      : `Due now (${tierLabel})`
    : wibClock
      ? `Next check ${wibClock} WIB (~${diffMinutes}m • ${tierLabel})`
      : `Next check in ~${diffMinutes}m (${tierLabel})`;

  const wibTime = wibClock
    ? isDueNow
      ? `Cooldown expired at ${wibClock} WIB (${tierLabel})`
      : `Eligible at ${wibClock} WIB (${tierLabel})`
    : '';

  return { nextCycleEta: etaStr, nextCycleTimestamp: wibTime };
}

