import { StarterPlayer, TeamFormation } from '@/lib/database';

export interface FormationNode {
  player: StarterPlayer;
  x: number;
  y: number;
  isHome: boolean;
}

export function parseFormationCounts(formation?: string): number[] {
  if (!formation) return [1, 4, 3, 3];
  const parts = formation.split('-').map((p) => parseInt(p.trim(), 10)).filter((n) => !isNaN(n));
  if (parts.length === 0) return [1, 4, 3, 3];
  return [1, ...parts]; // 1 GK prepended
}

export function getPositionRank(pos?: string): number {
  if (!pos) return 2;
  const p = pos.toUpperCase().trim();
  if (p === 'GK' || p === 'G' || p.includes('GOAL')) return 0;
  if (p === 'D' || p.includes('BACK') || p.includes('DEF') || ['CB', 'LB', 'RB', 'LWB', 'RWB'].includes(p)) return 1;
  if (p === 'M' || p.includes('MID') || ['CM', 'DM', 'AM', 'LM', 'RM'].includes(p)) return 2;
  return 3;
}

export function clampPitchCoordinate(
  x: number,
  y: number,
  isGK: boolean,
  isHome: boolean,
): [number, number] {
  if (isGK) {
    const clampedX = isHome ? 5.2 : 99.8;
    return [clampedX, 34.0];
  }
  const clampedX = Math.max(6.0, Math.min(99.0, x));
  const clampedY = Math.max(7.0, Math.min(61.0, y));
  return [clampedX, clampedY];
}

export function calculateStarterPositions(
  teamFormation?: TeamFormation | null,
  isHome: boolean = true,
): FormationNode[] {
  if (!teamFormation || !teamFormation.starters || teamFormation.starters.length === 0) {
    return [];
  }

  // Sort starters strictly by tactical positional hierarchy
  const starters = [...teamFormation.starters].sort(
    (a, b) => getPositionRank(a.position) - getPositionRank(b.position),
  );
  const lineCounts = parseFormationCounts(teamFormation.formation);
  const nodes: FormationNode[] = [];
  let playerIdx = 0;
  const totalLines = lineCounts.length;

  for (let lineIdx = 0; lineIdx < totalLines; lineIdx++) {
    const countInLine = lineCounts[lineIdx];
    const playersInLine = starters.slice(playerIdx, playerIdx + countInLine);
    playerIdx += countInLine;

    const isGK = lineIdx === 0;
    const lineRatio = totalLines > 1 ? lineIdx / (totalLines - 1) : 0;
    const rawX = isHome ? 5.2 + lineRatio * 41 : 99.8 - lineRatio * 41;

    playersInLine.forEach((p, j) => {
      const rawY = isGK ? 34.0 : 8 + ((52 * (j + 1)) / (countInLine + 1));
      const [safeX, safeY] = clampPitchCoordinate(rawX, rawY, isGK, isHome);
      nodes.push({ player: p, x: safeX, y: safeY, isHome });
    });
  }

  return nodes;
}
