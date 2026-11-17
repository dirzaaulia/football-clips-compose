'use client';

import React, { useState } from 'react';
import InfoHeroBanner from '@/components/info/InfoHeroBanner';
import SettingsHubCards from '@/components/info/SettingsHubCards';
import InfoKnowledgeCards from '@/components/info/InfoKnowledgeCards';
import MyClubsModal from '@/components/clubs/MyClubsModal';

export default function SettingsPage() {
  const [isMyClubsModalOpen, setIsMyClubsModalOpen] = useState(false);

  const handleOpenPaywall = () => {
    if (typeof window !== 'undefined') {
      window.dispatchEvent(new CustomEvent('fc-open-paywall'));
    }
  };

  return (
    <div className="max-w-[1600px] w-full mx-auto px-4 sm:px-6 lg:px-8 py-6 select-none space-y-6">
      <InfoHeroBanner />

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 items-start">
        <div className="lg:col-span-7">
          <SettingsHubCards
            onOpenMyClubs={() => setIsMyClubsModalOpen(true)}
            onOpenPaywall={handleOpenPaywall}
          />
        </div>

        <div className="lg:col-span-5">
          <InfoKnowledgeCards />
        </div>
      </div>

      <MyClubsModal
        isOpen={isMyClubsModalOpen}
        onClose={() => setIsMyClubsModalOpen(false)}
      />
    </div>
  );
}
