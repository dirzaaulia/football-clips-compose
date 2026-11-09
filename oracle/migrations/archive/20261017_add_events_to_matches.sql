-- Migration: 20261017_add_events_to_matches.sql
-- Description: Adds events JSONB column to matches table for chronological goals, cards, and substitutions.

ALTER TABLE matches ADD COLUMN IF NOT EXISTS events JSONB;
