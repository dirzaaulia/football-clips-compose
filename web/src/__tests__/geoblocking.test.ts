import { describe, expect, it } from 'vitest';
import { isRegionSupported, MatchRecord, RegionRestriction } from '@/lib/database';

function resolveWebPlayerStream(match: MatchRecord, userCountryCode: string) {
  const isPrimaryAllowed = isRegionSupported(match.youtube_region_info, userCountryCode);
  const isBackupAllowed = isRegionSupported(match.backup_youtube_region_info, userCountryCode);

  const canPlayPrimary = Boolean(match.highlight_video_id && isPrimaryAllowed);
  const canPlayBackup = Boolean(match.backup_highlight_video_id && isBackupAllowed);

  const activeVideoId = canPlayPrimary
    ? match.highlight_video_id
    : canPlayBackup
      ? match.backup_highlight_video_id
      : match.highlight_video_id;

  const isUsingBackup = !canPlayPrimary && canPlayBackup;
  const isAllBlocked = !canPlayPrimary && !canPlayBackup;

  return {
    activeVideoId,
    isUsingBackup,
    isAllBlocked,
  };
}

describe('Web Geoblocking & Dual-Stream Backup Failover', () => {
  const mockMatch: MatchRecord = {
    id: '401915001',
    competition_id: 'PL',
    competition_name: 'Premier League',
    utc_date: '2026-11-08T16:30:00Z',
    status: 'FINISHED',
    home_team: 'Arsenal',
    away_team: 'Chelsea',
    home_score: 3,
    away_score: 2,
    highlight_video_id: 'sky_uk_primary_123',
    backup_highlight_video_id: 'nbc_us_backup_456',
    youtube_region_info: { allowed: ['GB', 'IE'] },
    backup_youtube_region_info: { blocked: ['KP'] },
  };

  it('Scenario 1: Plays primary highlight when user region is allowed', () => {
    const state = resolveWebPlayerStream(mockMatch, 'GB');
    expect(state.activeVideoId).toBe('sky_uk_primary_123');
    expect(state.isUsingBackup).toBe(false);
    expect(state.isAllBlocked).toBe(false);
  });

  it('Scenario 2: Automatically fails over to backup stream when primary is blocked in user country', () => {
    const state = resolveWebPlayerStream(mockMatch, 'ID');
    expect(state.activeVideoId).toBe('nbc_us_backup_456');
    expect(state.isUsingBackup).toBe(true);
    expect(state.isAllBlocked).toBe(false);
  });

  it('Scenario 3: Flags isAllBlocked when both primary and backup are restricted in user country', () => {
    const state = resolveWebPlayerStream(mockMatch, 'KP');
    expect(state.isAllBlocked).toBe(true);
    expect(state.isUsingBackup).toBe(false);
  });

  it('Scenario 4: Treats null or empty RegionRestriction as globally watchable', () => {
    const globalRegion: RegionRestriction = {};
    expect(isRegionSupported(null, 'US')).toBe(true);
    expect(isRegionSupported(globalRegion, 'JP')).toBe(true);
    expect(isRegionSupported({ blocked: ['GB'] }, null)).toBe(true);
  });
});
