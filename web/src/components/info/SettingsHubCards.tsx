'use client';

import React, { useState } from 'react';
import {
  Shield,
  ChevronRight,
  ShoppingBag,
  Sparkles,
  Download,
  CheckCircle2,
  EyeOff,
  Eye,
} from 'lucide-react';
import { useMyClubs } from '@/hooks/useMyClubs';
import { useProStatus } from '@/hooks/useProStatus';
import { useSpoilerFree } from '@/hooks/useSpoilerFree';
import { useMerchandisePreference } from '@/hooks/useMerchandisePreference';
import SettingsAccountCard from './SettingsAccountCard';
import SettingsNotificationCard from './SettingsNotificationCard';
import { trackPaywallView, trackCheckUpdates } from '@/lib/analytics';

interface SettingsHubCardsProps {
  onOpenMyClubs: () => void;
  onOpenPaywall?: () => void;
}

function SpoilerFreePreferenceCard() {
  const { isSpoilerFree, toggleSpoilerFree } = useSpoilerFree();
  return (
    <div
      onClick={toggleSpoilerFree}
      className="p-4 rounded-2xl bg-[#1A1C22] border border-white/10 hover:border-[#DFFF00]/40 shadow-md flex items-center justify-between gap-3 cursor-pointer group transition-all"
    >
      <div className="flex items-center gap-3 min-w-0">
        <div className="w-10 h-10 rounded-xl bg-white/5 border border-white/10 flex items-center justify-center shrink-0 group-hover:scale-105 transition-transform">
          {isSpoilerFree ? (
            <EyeOff className="w-5 h-5 text-[#DFFF00]" />
          ) : (
            <Eye className="w-5 h-5 text-[#DFFF00]" />
          )}
        </div>
        <div className="min-w-0">
          <div className="text-sm font-bold text-white font-tech">Spoiler-Free Mode</div>
          <div className="text-xs text-white/50 truncate font-tech">
            {isSpoilerFree ? 'Scores & result spoilers hidden' : 'Hide match scores in feeds'}
          </div>
        </div>
      </div>
      <div
        className={`relative inline-flex h-6 w-11 shrink-0 items-center rounded-full transition-colors ${
          isSpoilerFree ? 'bg-[#DFFF00]' : 'bg-white/10'
        }`}
      >
        <span
          className={`inline-block h-4 w-4 transform rounded-full bg-[#0A0A0C] transition-transform ${
            isSpoilerFree ? 'translate-x-6' : 'translate-x-1'
          }`}
        />
      </div>
    </div>
  );
}

function MerchandisePreferenceCard({ onOpenPaywall }: { onOpenPaywall?: () => void }) {
  const isPro = useProStatus();
  const { rawPreference: merchandiseEnabled, setMerchandiseEnabled } =
    useMerchandisePreference();

  const handleToggle = () => {
    if (!isPro) {
      trackPaywallView('merchandise_setting');
      onOpenPaywall?.();
      return;
    }
    setMerchandiseEnabled(!merchandiseEnabled);
  };

  return (
    <div
      onClick={!isPro ? handleToggle : undefined}
      className="p-4 sm:p-5 rounded-2xl bg-[#1A1C22] border border-white/10 hover:border-white/20 shadow-md flex items-center justify-between gap-4 transition-all"
    >
      <div className="flex items-center gap-3.5 min-w-0">
        <div className="w-10 h-10 rounded-xl bg-[#DFFF00]/10 border border-[#DFFF00]/30 flex items-center justify-center shrink-0">
          <ShoppingBag className="w-5 h-5 text-[#DFFF00]" />
        </div>
        <div className="min-w-0">
          <div className="text-sm font-bold text-white font-tech">
            Merchandise Recommendations
          </div>
          <p className="text-xs text-white/50 truncate font-tech">
            Show official team gear links on Amazon
          </p>
        </div>
      </div>
      <div className="shrink-0">
        {isPro ? (
          <button
            type="button"
            onClick={handleToggle}
            className={`relative inline-flex h-6 w-11 items-center rounded-full transition-colors cursor-pointer ${
              merchandiseEnabled ? 'bg-[#DFFF00]' : 'bg-white/10'
            }`}
          >
            <span
              className={`inline-block h-4 w-4 transform rounded-full bg-[#0A0A0C] transition-transform ${
                merchandiseEnabled ? 'translate-x-6' : 'translate-x-1'
              }`}
            />
          </button>
        ) : (
          <button
            type="button"
            onClick={handleToggle}
            className="flex items-center gap-1.5 px-3 py-1.5 rounded-full bg-[#DFFF00]/15 border border-[#DFFF00]/30 hover:bg-[#DFFF00]/25 text-[#DFFF00] text-xs font-tech font-bold transition-all cursor-pointer"
          >
            <Sparkles className="w-3.5 h-3.5" />
            <span>Unlock</span>
          </button>
        )}
      </div>
    </div>
  );
}

export default function SettingsHubCards({
  onOpenMyClubs,
  onOpenPaywall,
}: SettingsHubCardsProps) {
  const { favoriteCount } = useMyClubs();
  const [checkedUpdates, setCheckedUpdates] = useState(false);

  const handleCheckUpdates = () => {
    trackCheckUpdates();
    setCheckedUpdates(true);
    setTimeout(() => setCheckedUpdates(false), 4000);
  };

  return (
    <div className="space-y-4">
      <SettingsAccountCard onOpenPaywall={onOpenPaywall} />

      <div className="grid grid-cols-1 sm:grid-cols-2 gap-3.5">
        <SpoilerFreePreferenceCard />

        <div
          onClick={onOpenMyClubs}
          className="p-4 rounded-2xl bg-[#1A1C22] border border-white/10 hover:border-[#DFFF00]/40 shadow-md flex items-center justify-between gap-3 cursor-pointer group transition-all"
        >
          <div className="flex items-center gap-3 min-w-0">
            <div className="w-10 h-10 rounded-xl bg-white/5 border border-white/10 flex items-center justify-center shrink-0 group-hover:scale-105 transition-transform">
              <Shield className="w-5 h-5 text-[#DFFF00]" />
            </div>
            <div className="min-w-0">
              <div className="text-sm font-bold text-white font-tech">My Clubs</div>
              <div
                className="text-xs text-white/50 truncate font-tech"
                suppressHydrationWarning
              >
                {favoriteCount > 0
                  ? `${favoriteCount} favorite club${favoriteCount > 1 ? 's' : ''} selected`
                  : 'Personalize tailored highlights'}
              </div>
            </div>
          </div>
          <div className="flex items-center gap-1 text-xs text-[#DFFF00] font-tech font-semibold shrink-0 group-hover:translate-x-0.5 transition-transform">
            <span>Manage</span>
            <ChevronRight className="w-4 h-4" />
          </div>
        </div>

        <a
          href="https://play.google.com/store/apps/details?id=com.dirzaaulia.footballclips"
          target="_blank"
          rel="noopener noreferrer"
          className="p-4 rounded-2xl bg-[#1A1C22] border border-white/10 hover:border-[#DFFF00]/40 shadow-md flex items-center justify-between gap-3 cursor-pointer group transition-all"
        >
          <div className="flex items-center gap-3 min-w-0">
            <div className="w-10 h-10 rounded-xl bg-white/5 border border-white/10 flex items-center justify-center shrink-0 group-hover:scale-105 transition-transform">
              <Download className="w-5 h-5 text-[#DFFF00]" />
            </div>
            <div className="min-w-0">
              <div className="text-sm font-bold text-white flex items-center gap-1.5 font-tech">
                <span>Android App</span>
                <span className="text-[9px] font-tech font-bold uppercase px-1.5 py-0.5 rounded bg-[#DFFF00]/15 text-[#DFFF00] border border-[#DFFF00]/30">
                  Official
                </span>
              </div>
              <div className="text-xs text-white/50 truncate font-tech">
                Get it on Google Play
              </div>
            </div>
          </div>
          <div className="flex items-center gap-1 text-xs text-[#DFFF00] font-tech font-semibold shrink-0 group-hover:translate-x-0.5 transition-transform">
            <span>Install</span>
            <ChevronRight className="w-4 h-4" />
          </div>
        </a>

        <div className="p-4 rounded-2xl bg-[#1A1C22] border border-white/10 shadow-md flex items-center justify-between gap-3">
          <div className="flex items-center gap-3 min-w-0">
            <div className="w-10 h-10 rounded-xl bg-white/5 border border-white/10 flex items-center justify-center shrink-0">
              <Download className="w-5 h-5 text-[#DFFF00]" />
            </div>
            <div className="min-w-0">
              <div className="text-sm font-bold text-white font-tech">
                App Updates &amp; Platform
              </div>
              <p className="text-xs text-white/50 truncate font-tech">
                FootballClips v3.0 (Oracle Edge Tier)
              </p>
            </div>
          </div>
          <button
            type="button"
            onClick={handleCheckUpdates}
            className="flex items-center gap-1.5 px-3 py-1.5 rounded-xl border border-white/10 hover:border-[#DFFF00]/40 text-xs font-tech font-bold text-white transition-all cursor-pointer shrink-0"
          >
            {checkedUpdates ? (
              <>
                <CheckCircle2 className="w-3.5 h-3.5 text-emerald-400" />
                <span className="text-emerald-400">Up to date</span>
              </>
            ) : (
              <span>Check Updates</span>
            )}
          </button>
        </div>
      </div>

      <MerchandisePreferenceCard onOpenPaywall={onOpenPaywall} />
      <SettingsNotificationCard />
    </div>
  );
}
