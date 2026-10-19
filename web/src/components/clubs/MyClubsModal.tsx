'use client';

import React, { useState, useMemo } from 'react';
import Image from 'next/image';
import { Search, Star, Sparkles, Check, X, Bell, Trophy } from 'lucide-react';
import { useMyClubs } from '@/hooks/useMyClubs';
import { useSupportedClubs } from '@/hooks/useSupportedClubs';
import { useCompetitions } from '@/hooks/useCompetitions';
import ModalSideSheet from '@/components/common/ModalSideSheet';
import CustomDropdown from '@/components/common/CustomDropdown';
import NotificationOptInModal from './NotificationOptInModal';
import { requestWebNotificationPermission, syncWebPushSubscription, getStoredWebPushToken } from '@/lib/firebase/messaging';

interface MyClubsModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export default function MyClubsModal({ isOpen, onClose }: MyClubsModalProps) {
  const { favoriteClubs, toggleClub, clearAll, favoriteCount } = useMyClubs();
  const allSupportedClubs = useSupportedClubs();
  const competitions = useCompetitions();
  const [search, setSearch] = useState('');
  const [selectedLeague, setSelectedLeague] = useState('ALL');
  const [isNotificationModalOpen, setIsNotificationModalOpen] = useState(false);
  const [isDenied, setIsDenied] = useState(false);
  const [notificationsEnabled, setNotificationsEnabled] = useState(() => {
    if (typeof window !== 'undefined' && 'Notification' in window) {
      return Notification.permission === 'granted';
    }
    return false;
  });

  const handleNotificationToggle = () => {
    if (typeof window === 'undefined' || !('Notification' in window)) return;
    if (Notification.permission === 'denied') {
      setIsDenied(true); setIsNotificationModalOpen(true); return;
    }
    if (Notification.permission === 'granted') return;
    setIsDenied(false); setIsNotificationModalOpen(true);
  };

  const handleEnableNotifications = async () => {
    const result = await requestWebNotificationPermission();
    if (result.granted) {
      setNotificationsEnabled(true);
      setIsNotificationModalOpen(false);
      if (result.token && favoriteClubs.size > 0) {
        await syncWebPushSubscription(result.token, Array.from(favoriteClubs), 'subscribe');
      }
    } else if (result.status === 'denied') {
      setIsDenied(true);
    }
  };

  const handleToggleClub = (clubId: string) => {
    const isAdding = !favoriteClubs.has(clubId);
    toggleClub(clubId);
    const token = getStoredWebPushToken();
    if (notificationsEnabled && token) {
      syncWebPushSubscription(token, [clubId], isAdding ? 'subscribe' : 'unsubscribe');
    }
    if (isAdding && !notificationsEnabled && typeof window !== 'undefined' && 'Notification' in window && Notification.permission === 'default') {
      setIsDenied(false);
      setIsNotificationModalOpen(true);
    }
  };

  const leagueTabs = useMemo(() => {
    return [
      { code: 'ALL', name: 'All Leagues' },
      ...competitions
        .filter((c) => c.competitionId !== 'ALL')
        .map((c) => ({ code: c.competitionId || '', name: c.name })),
    ];
  }, [competitions]);

  const filteredClubs = useMemo(() => {
    return allSupportedClubs.filter((club) => {
      const matchesLeague = selectedLeague === 'ALL' || club.leagueCode === selectedLeague;
      const s = search.toLowerCase().trim();
      const matchesSearch =
        !s ||
        club.name.toLowerCase().includes(s) ||
        club.shortName.toLowerCase().includes(s) ||
        (club.aliases && club.aliases.some((a) => a.toLowerCase().includes(s)));
      return matchesLeague && matchesSearch;
    });
  }, [allSupportedClubs, search, selectedLeague]);

  const titleHeader = (
    <div className="flex items-center justify-between w-full pr-3 gap-6">
      <div className="flex items-center gap-2.5 min-w-0">
        <Star className="w-5 h-5 text-[#7098F8] fill-[#7098F8] shrink-0" />
        <span className="font-extrabold text-sm sm:text-base text-white truncate">
          My Favorite Clubs
        </span>
        {favoriteCount > 0 && (
          <span className="px-2 py-0.5 rounded-full text-[10px] font-bold bg-[#1E3A8A] text-[#7098F8] border border-[#7098F8]/40 shrink-0">
            {favoriteCount}
          </span>
        )}
      </div>

      <button
        onClick={handleNotificationToggle}
        className={`ml-auto px-3 py-1.5 rounded-xl border flex items-center gap-1.5 text-xs font-bold transition-all cursor-pointer shrink-0 shadow-sm ${
          notificationsEnabled
            ? 'bg-blue-600/20 text-[#4FC3F7] border-[#4FC3F7]/40 hover:bg-blue-600/30'
            : 'bg-[#181C20] text-[#94A3B8] hover:text-white border-[#334155]'
        }`}
        title={notificationsEnabled ? 'Match alerts active' : 'Turn on match alerts'}
      >
        <Bell className={`w-3.5 h-3.5 ${notificationsEnabled ? 'fill-current text-[#4FC3F7]' : ''}`} />
        <span>{notificationsEnabled ? 'Alerts On' : 'Alerts'}</span>
      </button>
    </div>
  );

  return (
    <ModalSideSheet
      isOpen={isOpen}
      onClose={onClose}
      title={titleHeader}
      widthClass="sm:max-w-[480px] md:max-w-[520px]"
    >
      {/* Search & League Dropdown Controls */}
      <div className="p-4 border-b border-[#334155]/50 space-y-3 bg-[#101418]/60 shrink-0">
        <div className="relative">
          <Search className="w-4 h-4 text-[#94A3B8] absolute left-3.5 top-1/2 -translate-y-1/2" />
          <input
            type="text"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Search clubs (e.g. Arsenal, Madrid)..."
            className="w-full bg-[#181C20] border border-[#334155] rounded-xl pl-10 pr-9 py-2 text-xs text-white placeholder-[#94A3B8]/60 focus:outline-none focus:border-[#7098F8]"
          />
          {search && (
            <button
              onClick={() => setSearch('')}
              className="absolute right-3 top-1/2 -translate-y-1/2 text-[#94A3B8] hover:text-white"
            >
              <X className="w-3.5 h-3.5" />
            </button>
          )}
        </div>

        {/* Clean League Dropdown Filter */}
        <CustomDropdown<string>
          items={leagueTabs.map((tab) => ({
            value: tab.code,
            label: tab.name,
            icon: <Trophy className="w-3.5 h-3.5 text-[#7098F8]" />,
          }))}
          value={selectedLeague}
          onChange={setSelectedLeague}
          leadingIcon={<Trophy className="w-4 h-4 text-[#7098F8]" />}
          prefix="League"
          className="w-full"
          menuWidth="w-full"
          ariaLabel="Filter clubs by league"
        />
      </div>

      {/* Clubs List */}
      <div className="flex-1 overflow-y-auto p-4 space-y-2">
        {filteredClubs.length === 0 ? (
          <div className="py-12 text-center text-[#94A3B8] text-xs">
            No clubs match &quot;{search}&quot;.
          </div>
        ) : (
          filteredClubs.map((club) => {
            const isFav = favoriteClubs.has(club.id);
            return (
              <div
                key={club.id}
                onClick={() => handleToggleClub(club.id)}
                className={`flex items-center justify-between p-3 rounded-2xl border transition-all cursor-pointer select-none ${
                  isFav
                    ? 'bg-[#1E3A8A]/30 border-[#7098F8]/60 shadow-[0_0_12px_rgba(112,152,248,0.15)]'
                    : 'bg-[#1E2227] border-[#334155]/60 hover:border-[#7098F8]/40'
                }`}
              >
                <div className="flex items-center gap-3 min-w-0">
                  <div className="relative w-8 h-8 shrink-0 bg-black/40 rounded-lg p-1 border border-[#334155]/40 flex items-center justify-center">
                    <Image
                      src={club.crestUrl}
                      alt={club.name}
                      width={24}
                      height={24}
                      className="object-contain"
                      unoptimized
                    />
                  </div>
                  <div className="min-w-0">
                    <h4 className="text-xs sm:text-sm font-bold text-white truncate">
                      {club.name}
                    </h4>
                    <span className="text-[10px] text-[#94A3B8] uppercase tracking-wider font-semibold">
                      {club.leagueName}
                    </span>
                  </div>
                </div>

                <button
                  type="button"
                  aria-label={`Toggle ${club.name}`}
                  className={`p-1.5 rounded-xl border transition-all ${
                    isFav
                      ? 'bg-[#7098F8] text-[#101418] border-[#7098F8]'
                      : 'bg-[#181C20] text-[#94A3B8] border-[#334155] hover:text-white'
                  }`}
                >
                  <Star className={`w-3.5 h-3.5 ${isFav ? 'fill-current' : ''}`} />
                </button>
              </div>
            );
          })
        )}
      </div>

      {/* Footer */}
      <div className="p-4 border-t border-[#334155]/60 bg-[#101418] flex items-center justify-between gap-3 text-xs shrink-0">
        {favoriteCount > 0 ? (
          <button
            onClick={clearAll}
            className="text-[#94A3B8] hover:text-rose-400 font-semibold transition-colors cursor-pointer"
          >
            Clear all ({favoriteCount})
          </button>
        ) : (
          <span className="text-[#94A3B8] flex items-center gap-1.5">
            <Sparkles className="w-3.5 h-3.5 text-[#7098F8]" />
            Tap stars to follow
          </span>
        )}

        <button
          onClick={onClose}
          className="flex items-center gap-1.5 px-4 py-2 rounded-xl bg-gradient-to-r from-[#1E3A8A] to-[#7098F8] text-white font-bold hover:brightness-110 transition-all cursor-pointer shadow-sm"
        >
          <Check className="w-4 h-4" />
          <span>Done</span>
        </button>
      </div>

      <NotificationOptInModal
        isOpen={isNotificationModalOpen}
        onClose={() => setIsNotificationModalOpen(false)}
        onEnable={handleEnableNotifications}
        isDenied={isDenied}
      />
    </ModalSideSheet>
  );
}
