import type { FootballShortRecord } from './shorts';

/**
 * Diversifies a pool of Shorts using time-decay hype scoring, slight session jitter,
 * and a greedy anti-streak channel cooldown so consecutive videos never repeat the same channel.
 */
export function diversifyShortsFeed(
  records: FootballShortRecord[],
  limit: number,
  cooldown = 4,
): FootballShortRecord[] {
  if (records.length <= 1) return records.slice(0, limit);

  const nowMs = Date.now();
  const scored = records.map((item) => {
    const pubMs = Date.parse(item.published_at || '') || nowMs;
    const ageHours = Math.max(0, (nowMs - pubMs) / 3_600_000);
    const decay = Math.min(30, ageHours * 0.08);
    const jitter = Math.random() * 12;
    const baseHype = item.hype_score ?? 60;
    return { item, score: baseHype - decay + jitter };
  });

  scored.sort((a, b) => b.score - a.score);

  const remaining = [...scored];
  const result: FootballShortRecord[] = [];
  const recentChannels: string[] = [];

  while (result.length < limit && remaining.length > 0) {
    let chosenIdx = remaining.findIndex(
      (cand) => !recentChannels.includes(cand.item.channel_id),
    );
    if (chosenIdx === -1) {
      chosenIdx = pickLeastRecentChannelIndex(remaining, recentChannels);
    }
    const [picked] = remaining.splice(chosenIdx, 1);
    result.push(picked.item);
    recentChannels.push(picked.item.channel_id);
    if (recentChannels.length > cooldown) {
      recentChannels.shift();
    }
  }

  return result;
}

function pickLeastRecentChannelIndex(
  remaining: Array<{ item: FootballShortRecord; score: number }>,
  recentChannels: string[],
): number {
  let bestIdx = 0;
  let oldestPos = recentChannels.length;
  for (let i = 0; i < remaining.length; i++) {
    const pos = recentChannels.lastIndexOf(remaining[i].item.channel_id);
    if (pos < oldestPos) {
      oldestPos = pos;
      bestIdx = i;
    }
  }
  return bestIdx;
}
