'use client';

import React from 'react';
import {
  Heart,
  Globe,
  Coffee,
  Database,
  UserCheck,
  ShoppingBag,
  Scale,
  ShieldAlert,
} from 'lucide-react';
import CollapsibleCard from './CollapsibleCard';
import ExpressiveLinkCard from './ExpressiveLinkCard';
import { useMerchandisePreference } from '@/hooks/useMerchandisePreference';

function SupportDeveloperSection() {
  return (
    <CollapsibleCard
      title="Support Developer"
      icon={<Heart className="w-4 h-4 text-[#DFFF00]" />}
      initialExpanded={true}
    >
      <div className="space-y-3.5">
        <p className="text-xs text-white/70 font-tech leading-relaxed">
          If Football Highlights &amp; Clips brings you joy, consider supporting independent
          development:
        </p>
        <div className="grid grid-cols-1 gap-2.5">
          <ExpressiveLinkCard
            title="Website"
            subtitle="dirzaaulia.com"
            icon={<Globe className="w-4 h-4 text-[#DFFF00]" />}
            href="https://dirzaaulia.com"
          />
          <ExpressiveLinkCard
            title="Buy me a Coffee"
            subtitle="ko-fi.com/dirzaaulia"
            icon={<Coffee className="w-4 h-4 text-[#DFFF00]" />}
            href="https://ko-fi.com/dirzaaulia"
          />
          <ExpressiveLinkCard
            title="Saweria"
            subtitle="saweria.co/dirzaaulia"
            icon={<Heart className="w-4 h-4 text-[#DFFF00]" />}
            href="https://saweria.co/dirzaaulia"
          />
        </div>
      </div>
    </CollapsibleCard>
  );
}

function DataSourceAndAccountSection() {
  return (
    <>
      <CollapsibleCard
        title="Data Source"
        icon={<Database className="w-4 h-4 text-[#DFFF00]" />}
        initialExpanded={true}
      >
        <p className="text-xs text-white/70 leading-relaxed">
          All football match highlights are embedded strictly from verified official league
          and club channels on{' '}
          <a
            href="https://www.youtube.com/t/terms"
            target="_blank"
            rel="noopener noreferrer"
            className="text-[#DFFF00] font-bold underline hover:text-white transition-colors"
          >
            YouTube
          </a>{' '}
          in full compliance with YouTube API Services. Fixtures, live scores, and tables are
          powered by{' '}
          <a
            href="https://www.espn.com/soccer"
            target="_blank"
            rel="noopener noreferrer"
            className="text-[#DFFF00] font-bold underline hover:text-white transition-colors"
          >
            ESPN Live Sports Engine
          </a>
          . All team crests and competition emblems are delivered through verified official
          media CDN assets with edge caching.
        </p>
      </CollapsibleCard>

      <CollapsibleCard
        title="Account & Purchases"
        icon={<UserCheck className="w-4 h-4 text-[#DFFF00]" />}
        initialExpanded={false}
      >
        <div className="space-y-2.5 text-xs text-white/70 leading-relaxed">
          <p>
            Signing in is strictly for linking your Premium &apos;Remove Ads&apos; entitlement
            across devices. We do not collect, store, or sell any personal information. If you
            have previously purchased Premium on another device, simply sign in with the same
            account to restore your access.
          </p>
          <p className="text-white/50">
            • Purchases are tied to your store account (Google Play or Web Checkout) and
            linked to your Football Highlights &amp; Clips account upon login.
          </p>
        </div>
      </CollapsibleCard>
    </>
  );
}

function LegalAndPrivacySection({ showAffiliate }: { showAffiliate: boolean }) {
  return (
    <>
      {showAffiliate && (
        <CollapsibleCard
          title="Affiliate Disclosure"
          icon={<ShoppingBag className="w-4 h-4 text-[#DFFF00]" />}
          initialExpanded={false}
        >
          <div className="space-y-2 text-xs text-white/70 leading-relaxed">
            <p>
              Football Highlights &amp; Clips is a participant in the Amazon Services LLC
              Associates Program, an affiliate advertising program designed to provide a means
              for sites to earn advertising fees by advertising and linking to Amazon.com and
              affiliated international marketplaces.
            </p>
            <p className="text-white/50">
              When you click on links to official team merchandise or equipment and make a
              purchase, this app may earn an affiliate commission at no additional cost to you.
              You can disable merchandise recommendations at any time in the settings.
            </p>
          </div>
        </CollapsibleCard>
      )}

      <CollapsibleCard
        title="Disclaimer"
        icon={<Scale className="w-4 h-4 text-[#DFFF00]" />}
        initialExpanded={false}
      >
        <p className="text-xs text-white/70 leading-relaxed">
          All team/league names, logos, and brands used in this application are the property
          of their respective owners. They are used strictly for identification and
          informational purposes under fair use. This application is unofficial and is not
          affiliated with, sponsored by, or endorsed by any football club, league, or
          broadcaster.
        </p>
      </CollapsibleCard>

      <CollapsibleCard
        title="Privacy Policy & Terms"
        icon={<ShieldAlert className="w-4 h-4 text-[#DFFF00]" />}
        initialExpanded={false}
      >
        <div className="space-y-3">
          <p className="text-xs text-white/70 leading-relaxed">
            Football Highlights &amp; Clips uses official YouTube API Services and player
            embeds. By accessing or using this service, you agree to be bound by the YouTube
            Terms of Service and Google Privacy Policy. Review our full legal documentation
            below:
          </p>
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-2.5">
            <ExpressiveLinkCard
              title="Privacy Policy"
              subtitle="fc.dirzaaulia.com/privacy"
              icon={<ShieldAlert className="w-4 h-4 text-[#DFFF00]" />}
              href="/privacy"
            />
            <ExpressiveLinkCard
              title="Terms & Conditions"
              subtitle="fc.dirzaaulia.com/tnc"
              icon={<Scale className="w-4 h-4 text-[#DFFF00]" />}
              href="/tnc"
            />
          </div>
        </div>
      </CollapsibleCard>
    </>
  );
}

export default function InfoKnowledgeCards() {
  const { isMerchandiseEnabled } = useMerchandisePreference();

  return (
    <div className="space-y-4">
      <SupportDeveloperSection />
      <DataSourceAndAccountSection />
      <LegalAndPrivacySection showAffiliate={isMerchandiseEnabled} />
    </div>
  );
}
