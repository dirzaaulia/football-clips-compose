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

export function clampPitchCoordinate(
  x: number,
  y: number,
  isGK: boolean,
  isHome: boolean
): [number, number] {
  if (isGK) {
    const minX = isHome ? 4.5 : 92.0;
    const maxX = isHome ? 12.0 : 100.5;
    const clampedX = Math.max(minX, Math.min(maxX, x));
    const clampedY = Math.max(24.0, Math.min(44.0, y));
    return [clampedX, clampedY];
  }
  // Outfield players must strictly stay inside pitch boundary lines
  const clampedX = Math.max(7.0, Math.min(95.0, x));
  const clampedY = Math.max(6.0, Math.min(62.0, y));
  return [clampedX, clampedY];
}

export function calculateStarterPositions(
  teamFormation?: TeamFormation | null,
  isHome: boolean = true
): FormationNode[] {
  if (!teamFormation || !teamFormation.starters || teamFormation.starters.length === 0) {
    return [];
  }

  const starters = teamFormation.starters;
  const lineCounts = parseFormationCounts(teamFormation.formation);
  const nodes: FormationNode[] = [];
  let playerIdx = 0;
  const totalLines = lineCounts.length;

  for (let lineIdx = 0; lineIdx < totalLines; lineIdx++) {
    const countInLine = lineCounts[lineIdx];
    const playersInLine = starters.slice(playerIdx, playerIdx + countInLine);
    playerIdx += countInLine;

    const lineRatio = totalLines > 1 ? lineIdx / (totalLines - 1) : 0;
    const rawX = isHome ? 5 + lineRatio * 40 : 100 - lineRatio * 40;
    const isGK = lineIdx === 0;

    playersInLine.forEach((p, j) => {
      const rawY = (68 * (j + 1)) / (countInLine + 1);
      const [safeX, safeY] = clampPitchCoordinate(rawX, rawY, isGK, isHome);
      nodes.push({ player: p, x: safeX, y: safeY, isHome });
    });
  }

  return nodes;
}
