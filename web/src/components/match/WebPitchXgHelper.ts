import { WebPitchPlay } from './WebPitchRadar';
import { FormationNode } from './WebPitchFormationHelper';

export interface PassingLink {
  fromX: number;
  fromY: number;
  toX: number;
  toY: number;
  isHome: boolean;
  weight: number;
  fromName: string;
  toName: string;
}

export interface MomentumPoint {
  minute: number;
  netMomentum: number;
  homeValue: number;
  awayValue: number;
}

export function calculateShotXg(play: WebPitchPlay, isHome: boolean): number {
  const t = (play.type_text || play.type || '').toLowerCase();
  const desc = (play.text || '').toLowerCase();

  if (desc.includes('penalty')) return 0.78;

  const targetX = isHome ? 100 : 0;
  const targetY = 50;
  const dx = (play.x_percent - targetX) * 1.05;
  const dy = (play.y_percent - targetY) * 0.68;
  const distance = Math.max(1.5, Math.sqrt(dx * dx + dy * dy));

  const dy1 = (play.y_percent - 44.8) * 0.68;
  const dy2 = (play.y_percent - 55.2) * 0.68;
  const a1 = Math.atan2(dy1, Math.abs(dx));
  const a2 = Math.atan2(dy2, Math.abs(dx));
  const angle = Math.abs(a1 - a2);

  let baseXg = Math.min(0.92, (angle * 0.45) / Math.pow(distance * 0.15, 1.25));

  if (t.includes('header') || desc.includes('header')) {
    baseXg *= 0.58;
  }
  if (desc.includes('six yard') || distance < 6.5) {
    baseXg = Math.max(0.42, baseXg);
  }
  if (t.includes('goal')) {
    baseXg = Math.max(0.22, baseXg);
  }

  return Math.max(0.02, Math.min(0.92, Math.round(baseXg * 100) / 100));
}

export function calculateShotXgot(xg: number, goalPosY: number | null | undefined): number {
  if (goalPosY == null) return xg;
  if (goalPosY < 43.5 || goalPosY > 56.5) return 0.0;

  const distFromCenter = Math.abs(goalPosY - 50.0);
  const cornerBonus = (distFromCenter / 5.5) * 0.35;
  return Math.min(0.98, Math.round((xg * 0.85 + 0.15 + cornerBonus) * 100) / 100);
}

export function computeAttackMomentum(plays: WebPitchPlay[], totalMinutes = 95): MomentumPoint[] {
  const buckets: { home: number; away: number }[] = Array.from({ length: totalMinutes }, () => ({
    home: 0,
    away: 0,
  }));

  for (const p of plays) {
    const minMatch = p.clock.match(/(\d+)/);
    const m = minMatch ? Math.min(totalMinutes - 1, parseInt(minMatch[1], 10)) : 0;
    const t = (p.type_text || p.type || '').toLowerCase();
    const isHome = p.period !== 2;

    let weight = 1.0;
    if (t.includes('goal')) weight = 5.0;
    else if (t.includes('on target') || t.includes('saved')) weight = 3.0;
    else if (t.includes('shot') || t.includes('attempt')) weight = 2.0;
    else if (t.includes('corner')) weight = 1.5;

    if (isHome) buckets[m].home += weight;
    else buckets[m].away += weight;
  }

  return buckets.map((b, idx) => {
    let smoothHome = 0;
    let smoothAway = 0;
    let count = 0;
    for (let offset = -2; offset <= 2; offset++) {
      const cur = idx + offset;
      if (cur >= 0 && cur < totalMinutes) {
        smoothHome += buckets[cur].home;
        smoothAway += buckets[cur].away;
        count++;
      }
    }
    const h = smoothHome / (count || 1);
    const a = smoothAway / (count || 1);
    const net = Math.max(-10, Math.min(10, Math.round((h - a) * 10) / 10));
    return {
      minute: idx,
      netMomentum: net,
      homeValue: Math.round(h * 10) / 10,
      awayValue: Math.round(a * 10) / 10,
    };
  });
}

export function derivePassingNetworkEdges(
  homeNodes: FormationNode[],
  awayNodes: FormationNode[],
): PassingLink[] {
  const links: PassingLink[] = [];

  const connectTeam = (nodes: FormationNode[], isHome: boolean) => {
    const sorted = [...nodes].sort((a, b) => a.x - b.x);
    for (let i = 0; i < sorted.length; i++) {
      for (let j = i + 1; j < sorted.length; j++) {
        const dx = sorted[i].x - sorted[j].x;
        const dy = sorted[i].y - sorted[j].y;
        const dist = Math.sqrt(dx * dx + dy * dy);
        if (dist > 6 && dist < 26) {
          links.push({
            fromX: sorted[i].x,
            fromY: sorted[i].y,
            toX: sorted[j].x,
            toY: sorted[j].y,
            isHome,
            weight: Math.max(0.2, 1 - dist / 28),
            fromName: sorted[i].player.name,
            toName: sorted[j].player.name,
          });
        }
      }
    }
  };

  connectTeam(homeNodes, true);
  connectTeam(awayNodes, false);
  return links;
}
