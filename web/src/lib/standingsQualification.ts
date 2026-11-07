export interface QualificationZone {
  label: string;
  badgeClass: string;
  dotColor: string;
  isPositionInZone: (pos: number, totalTeams: number) => boolean;
}

export function getQualificationZones(competitionCode?: string, totalTeams = 20): QualificationZone[] {
  const code = (competitionCode || 'PL').toUpperCase();

  switch (code) {
    case 'PL':
    case 'PD':
    case 'SA':
      return [
        {
          label: 'Champions League (1-4)',
          badgeClass: 'bg-[#1E3A8A]/50 text-[#7098F8] border border-[#7098F8]/40 shadow-sm',
          dotColor: '#7098F8',
          isPositionInZone: (pos) => pos >= 1 && pos <= 4,
        },
        {
          label: 'Europa League (5)',
          badgeClass: 'bg-amber-950/40 text-amber-400 border border-amber-800/40',
          dotColor: '#F59E0B',
          isPositionInZone: (pos) => pos === 5,
        },
        {
          label: 'Conference League (6)',
          badgeClass: 'bg-emerald-950/40 text-emerald-400 border border-emerald-800/40',
          dotColor: '#10B981',
          isPositionInZone: (pos) => pos === 6,
        },
        {
          label: 'Relegation',
          badgeClass: 'bg-rose-950/40 text-rose-400 border border-rose-800/40',
          dotColor: '#EF4444',
          isPositionInZone: (pos, total) => total > 4 && pos > total - 3,
        },
      ];

    case 'BL1':
      return [
        {
          label: 'Champions League (1-4)',
          badgeClass: 'bg-[#1E3A8A]/50 text-[#7098F8] border border-[#7098F8]/40 shadow-sm',
          dotColor: '#7098F8',
          isPositionInZone: (pos) => pos >= 1 && pos <= 4,
        },
        {
          label: 'Europa League (5)',
          badgeClass: 'bg-amber-950/40 text-amber-400 border border-amber-800/40',
          dotColor: '#F59E0B',
          isPositionInZone: (pos) => pos === 5,
        },
        {
          label: 'Conference League (6)',
          badgeClass: 'bg-emerald-950/40 text-emerald-400 border border-emerald-800/40',
          dotColor: '#10B981',
          isPositionInZone: (pos) => pos === 6,
        },
        {
          label: 'Relegation Playoff (16)',
          badgeClass: 'bg-rose-900/30 text-rose-300 border border-rose-700/40',
          dotColor: '#E11D48',
          isPositionInZone: (pos) => pos === 16,
        },
        {
          label: 'Relegation (17-18)',
          badgeClass: 'bg-rose-950/40 text-rose-400 border border-rose-800/40',
          dotColor: '#EF4444',
          isPositionInZone: (pos) => pos >= 17 && pos <= 18,
        },
      ];

    case 'FL1':
      return [
        {
          label: 'Champions League (1-3)',
          badgeClass: 'bg-[#1E3A8A]/50 text-[#7098F8] border border-[#7098F8]/40 shadow-sm',
          dotColor: '#7098F8',
          isPositionInZone: (pos) => pos >= 1 && pos <= 3,
        },
        {
          label: 'UCL Qualifiers (4)',
          badgeClass: 'bg-cyan-950/40 text-cyan-400 border border-cyan-800/40',
          dotColor: '#06B6D4',
          isPositionInZone: (pos) => pos === 4,
        },
        {
          label: 'Europa League (5)',
          badgeClass: 'bg-amber-950/40 text-amber-400 border border-amber-800/40',
          dotColor: '#F59E0B',
          isPositionInZone: (pos) => pos === 5,
        },
        {
          label: 'Conference League (6)',
          badgeClass: 'bg-emerald-950/40 text-emerald-400 border border-emerald-800/40',
          dotColor: '#10B981',
          isPositionInZone: (pos) => pos === 6,
        },
        {
          label: 'Relegation Playoff (16)',
          badgeClass: 'bg-rose-900/30 text-rose-300 border border-rose-700/40',
          dotColor: '#E11D48',
          isPositionInZone: (pos) => pos === 16,
        },
        {
          label: 'Relegation (17-18)',
          badgeClass: 'bg-rose-950/40 text-rose-400 border border-rose-800/40',
          dotColor: '#EF4444',
          isPositionInZone: (pos) => pos >= 17 && pos <= 18,
        },
      ];

    case 'DED':
      return [
        {
          label: 'Champions League (1-2)',
          badgeClass: 'bg-[#1E3A8A]/50 text-[#7098F8] border border-[#7098F8]/40 shadow-sm',
          dotColor: '#7098F8',
          isPositionInZone: (pos) => pos >= 1 && pos <= 2,
        },
        {
          label: 'UCL Qualifiers (3)',
          badgeClass: 'bg-cyan-950/40 text-cyan-400 border border-cyan-800/40',
          dotColor: '#06B6D4',
          isPositionInZone: (pos) => pos === 3,
        },
        {
          label: 'Europa League (4)',
          badgeClass: 'bg-amber-950/40 text-amber-400 border border-amber-800/40',
          dotColor: '#F59E0B',
          isPositionInZone: (pos) => pos === 4,
        },
        {
          label: 'UECL Playoff (5-8)',
          badgeClass: 'bg-emerald-950/40 text-emerald-400 border border-emerald-800/40',
          dotColor: '#10B981',
          isPositionInZone: (pos) => pos >= 5 && pos <= 8,
        },
        {
          label: 'Relegation Playoff (16)',
          badgeClass: 'bg-rose-900/30 text-rose-300 border border-rose-700/40',
          dotColor: '#E11D48',
          isPositionInZone: (pos) => pos === 16,
        },
        {
          label: 'Relegation (17-18)',
          badgeClass: 'bg-rose-950/40 text-rose-400 border border-rose-800/40',
          dotColor: '#EF4444',
          isPositionInZone: (pos) => pos >= 17 && pos <= 18,
        },
      ];

    case 'PPL':
      return [
        {
          label: 'Champions League (1)',
          badgeClass: 'bg-[#1E3A8A]/50 text-[#7098F8] border border-[#7098F8]/40 shadow-sm',
          dotColor: '#7098F8',
          isPositionInZone: (pos) => pos === 1,
        },
        {
          label: 'UCL Qualifiers (2)',
          badgeClass: 'bg-cyan-950/40 text-cyan-400 border border-cyan-800/40',
          dotColor: '#06B6D4',
          isPositionInZone: (pos) => pos === 2,
        },
        {
          label: 'Europa League (3)',
          badgeClass: 'bg-amber-950/40 text-amber-400 border border-amber-800/40',
          dotColor: '#F59E0B',
          isPositionInZone: (pos) => pos === 3,
        },
        {
          label: 'UECL Qualifiers (4-5)',
          badgeClass: 'bg-emerald-950/40 text-emerald-400 border border-emerald-800/40',
          dotColor: '#10B981',
          isPositionInZone: (pos) => pos >= 4 && pos <= 5,
        },
        {
          label: 'Relegation Playoff (16)',
          badgeClass: 'bg-rose-900/30 text-rose-300 border border-rose-700/40',
          dotColor: '#E11D48',
          isPositionInZone: (pos) => pos === 16,
        },
        {
          label: 'Relegation (17-18)',
          badgeClass: 'bg-rose-950/40 text-rose-400 border border-rose-800/40',
          dotColor: '#EF4444',
          isPositionInZone: (pos) => pos >= 17 && pos <= 18,
        },
      ];

    case 'SPL':
      return [
        {
          label: 'AFC Champions League Elite (1-3)',
          badgeClass: 'bg-[#1E3A8A]/50 text-[#7098F8] border border-[#7098F8]/40 shadow-sm',
          dotColor: '#7098F8',
          isPositionInZone: (pos) => pos >= 1 && pos <= 3,
        },
        {
          label: 'Relegation',
          badgeClass: 'bg-rose-950/40 text-rose-400 border border-rose-800/40',
          dotColor: '#EF4444',
          isPositionInZone: (pos, total) => total > 3 && pos > total - 3,
        },
      ];

    case 'MLS':
      return [
        {
          label: 'MLS Cup Playoffs (1-7)',
          badgeClass: 'bg-[#1E3A8A]/50 text-[#7098F8] border border-[#7098F8]/40 shadow-sm',
          dotColor: '#7098F8',
          isPositionInZone: (pos) => pos >= 1 && pos <= 7,
        },
        {
          label: 'Wild Card (8-9)',
          badgeClass: 'bg-purple-950/40 text-purple-400 border border-purple-800/40',
          dotColor: '#8B5CF6',
          isPositionInZone: (pos) => pos >= 8 && pos <= 9,
        },
      ];

    case 'IDN':
      return [
        {
          label: 'AFC Champions League Two (1)',
          badgeClass: 'bg-[#1E3A8A]/50 text-[#7098F8] border border-[#7098F8]/40 shadow-sm',
          dotColor: '#7098F8',
          isPositionInZone: (pos) => pos === 1,
        },
        {
          label: 'AFC Challenge League (2)',
          badgeClass: 'bg-emerald-950/40 text-emerald-400 border border-emerald-800/40',
          dotColor: '#10B981',
          isPositionInZone: (pos) => pos === 2,
        },
        {
          label: 'Relegation',
          badgeClass: 'bg-rose-950/40 text-rose-400 border border-rose-800/40',
          dotColor: '#EF4444',
          isPositionInZone: (pos, total) => total > 3 && pos > total - 3,
        },
      ];

    case 'BRA':
      return [
        {
          label: 'Copa Libertadores (1-4)',
          badgeClass: 'bg-[#1E3A8A]/50 text-[#7098F8] border border-[#7098F8]/40 shadow-sm',
          dotColor: '#7098F8',
          isPositionInZone: (pos) => pos >= 1 && pos <= 4,
        },
        {
          label: 'Libertadores Qualifiers (5-6)',
          badgeClass: 'bg-cyan-950/40 text-cyan-400 border border-cyan-800/40',
          dotColor: '#06B6D4',
          isPositionInZone: (pos) => pos >= 5 && pos <= 6,
        },
        {
          label: 'Copa Sudamericana (7-12)',
          badgeClass: 'bg-amber-950/40 text-amber-400 border border-amber-800/40',
          dotColor: '#F59E0B',
          isPositionInZone: (pos) => pos >= 7 && pos <= 12,
        },
        {
          label: 'Relegation (17-20)',
          badgeClass: 'bg-rose-950/40 text-rose-400 border border-rose-800/40',
          dotColor: '#EF4444',
          isPositionInZone: (pos, total) => total >= 20 && pos >= 17 && pos <= 20,
        },
      ];

    case 'ARG':
      return [
        {
          label: 'Copa Libertadores (1-3)',
          badgeClass: 'bg-[#1E3A8A]/50 text-[#7098F8] border border-[#7098F8]/40 shadow-sm',
          dotColor: '#7098F8',
          isPositionInZone: (pos) => pos >= 1 && pos <= 3,
        },
        {
          label: 'Copa Sudamericana (4-9)',
          badgeClass: 'bg-amber-950/40 text-amber-400 border border-amber-800/40',
          dotColor: '#F59E0B',
          isPositionInZone: (pos) => pos >= 4 && pos <= 9,
        },
        {
          label: 'Relegation',
          badgeClass: 'bg-rose-950/40 text-rose-400 border border-rose-800/40',
          dotColor: '#EF4444',
          isPositionInZone: (pos, total) => total > 2 && pos > total - 2,
        },
      ];

    case 'CL':
    case 'EL':
    case 'ECL':
      return [
        {
          label: 'Round of 16 (1-8)',
          badgeClass: 'bg-[#1E3A8A]/50 text-[#7098F8] border border-[#7098F8]/40 shadow-sm',
          dotColor: '#7098F8',
          isPositionInZone: (pos) => pos >= 1 && pos <= 8,
        },
        {
          label: 'Knockout Play-offs (9-24)',
          badgeClass: 'bg-purple-950/40 text-purple-400 border border-purple-800/40',
          dotColor: '#8B5CF6',
          isPositionInZone: (pos) => pos >= 9 && pos <= 24,
        },
      ];

    default:
      return [
        {
          label: 'Top Tier (1-4)',
          badgeClass: 'bg-[#1E3A8A]/50 text-[#7098F8] border border-[#7098F8]/40 shadow-sm',
          dotColor: '#7098F8',
          isPositionInZone: (pos) => pos >= 1 && pos <= 4,
        },
        {
          label: 'Relegation',
          badgeClass: 'bg-rose-950/40 text-rose-400 border border-rose-800/40',
          dotColor: '#EF4444',
          isPositionInZone: (pos, total) => total > 4 && pos > total - 3,
        },
      ];
  }
}

export function getZoneForPosition(competitionCode: string | undefined, position: number, totalTeams: number): QualificationZone | undefined {
  return getQualificationZones(competitionCode, totalTeams).find((z) => z.isPositionInZone(position, totalTeams));
}
