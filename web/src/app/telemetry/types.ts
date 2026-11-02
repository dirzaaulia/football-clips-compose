export interface QuotaStatus {
  limit: number;
  remaining: number;
  hits_today: number;
  pt_date: string;
  hours_until_reset: number;
  is_exhausted: boolean;
  source?: string;
}

export interface PipelineInfo {
  cycle_index?: number;
  sweep_index?: number;
  cadence: string;
  status: string;
  last_sync?: string;
  last_sweep?: string;
  mode: string;
}

export interface TelemetryCycle {
  id: number;
  cycle_index: number;
  timestamp: string;
  created_at: string;
  mode: string;
  pipeline: string;
  subsystem?: string;
  next_sleep?: string;
  summary?: {
    total_events?: number;
    matches_checked?: number;
    matches_linked?: number;
    cooldown_count?: number;
    status?: string;
  };
}

export interface TelemetryEvent {
  id?: number;
  cycle_id?: number;
  pipeline?: string;
  subsystem?: string;
  event_text: string;
  level?: string;
  code?: string;
  match_id?: string | number | null;
  attributes?: Record<string, unknown>;
  created_at?: string;
}

export interface CandidateVerdict {
  candidateId: string;
  index: number;
  title: string;
  channel: string;
  url: string;
  isAccepted: boolean;
  verdictReason: string;
  evaluator: string;
  score: number;
}

export interface MatchWaterfall {
  matchId: string;
  title: string;
  date: string;
  status: 'LINKED' | 'COOLDOWN' | 'UNLINKED' | 'SEARCHED';
  step1Channels: string[];
  step2Candidates: string[];
  step3Verdicts: CandidateVerdict[];
  step4Outcome: string;
  outcomeVideoUrl?: string;
  outcomeVideoId?: string;
  cooldownReason?: string;
  cooldownRemainingMinutes?: number;
  stoppedAtStep?: 1 | 2 | 3 | 4;
  stoppedReason?: string;
}

export interface TelemetryPayload {
  updated_at: string;
  cycle_count: number;
  quota: QuotaStatus;
  pipelines: Record<string, PipelineInfo>;
  cycles: TelemetryCycle[];
  events: TelemetryEvent[];
}

export interface NotificationItem {
  id: string;
  timestamp: string;
  event_type: string;
  title: string;
  body: string;
  match_id: string;
  home_team: string;
  away_team: string;
  club_ids: number[];
  channel_id: string;
  audience?: {
    total_devices?: number;
    distinct_users?: number;
    platform_breakdown?: { android?: number; web?: number };
    target_tokens?: number;
    success_count?: number;
    failure_count?: number;
    total_recipients?: number;
  };
}

export interface NotificationsPayload {
  updated_at: string;
  count: number;
  notifications: NotificationItem[];
}

export interface SubscriptionItem {
  id: string;
  timestamp: string;
  platform: string;
  device_name: string;
  action: string;
  club_ids: number[];
}

export interface SubscriptionsPayload {
  updated_at: string;
  items: SubscriptionItem[];
}

export interface UserAnalyticsEvent {
  id: number;
  event_name: string;
  user_id?: string | null;
  device_id?: string | null;
  platform: string;
  payload: Record<string, any>;
  created_at: string;
}

export type TelemetryTab = 'highlights' | 'fixtures' | 'websub' | 'notifications' | 'analytics';
