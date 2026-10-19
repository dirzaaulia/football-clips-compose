import React from 'react';
import { Heart, Database, ShieldCheck, Scale, FileText, Globe, Coffee, HeartHandshake } from 'lucide-react';
import InfoHeroBanner from '@/components/info/InfoHeroBanner';
import CollapsibleCard from '@/components/info/CollapsibleCard';
import ExpressiveLinkCard from '@/components/info/ExpressiveLinkCard';
import Link from 'next/link';

export const metadata = {
  title: 'Information & Resources | Football Highlights & Clips',
  description: 'Verified data sources, independent development, cross-platform billing, and privacy documentation.',
};

export default function InfoPage() {
  return (
    <div className="max-w-4xl mx-auto px-4 sm:px-6 py-6 space-y-5">
      <InfoHeroBanner />

      {/* 1. Support Developer Card */}
      <CollapsibleCard
        title="Support Developer"
        icon={<Heart className="w-5 h-5 text-[#7098F8]" />}
        initialExpanded={true}
      >
        <div className="space-y-4 pt-1">
          <p className="text-center sm:text-left">
            If Football Highlights & Clips brings you joy, consider supporting independent development:
          </p>
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
            <ExpressiveLinkCard
              title="Website"
              subtitle="dirzaaulia.com"
              icon={<Globe className="w-4 h-4 text-[#7098F8]" />}
              href="https://dirzaaulia.com"
            />
            <ExpressiveLinkCard
              title="Buy me a Coffee"
              subtitle="ko-fi.com/dirzaaulia"
              icon={<Coffee className="w-4 h-4 text-[#7098F8]" />}
              href="https://ko-fi.com/dirzaaulia"
            />
            <ExpressiveLinkCard
              title="Saweria"
              subtitle="saweria.co/dirzaaulia"
              icon={<HeartHandshake className="w-4 h-4 text-[#7098F8]" />}
              href="https://saweria.co/dirzaaulia"
            />
          </div>
        </div>
      </CollapsibleCard>

      {/* 2. Data Sources Card */}
      <CollapsibleCard
        title="Data Sources & Integrity"
        icon={<Database className="w-5 h-5 text-[#7098F8]" />}
        initialExpanded={false}
      >
        <div className="space-y-2 pt-1">
          <p>
            All match highlights are embedded strictly from verified, official league and club channels on YouTube in full compliance with the official YouTube IFrame Player API. Live match fixtures, scores, and schedules are provided by the ESPN Live Sports Engine and official league feeds.
          </p>
          <p>
            All team and league crests are retrieved from verified official club assets and sports media networks. Match tactical debriefs are powered by advanced AI sports intelligence.
          </p>
        </div>
      </CollapsibleCard>

      {/* 3. Account & Purchases Card */}
      <CollapsibleCard
        title="Account & Purchases"
        icon={<ShieldCheck className="w-5 h-5 text-[#7098F8]" />}
        initialExpanded={false}
      >
        <div className="space-y-2 pt-1">
          <p>
            Signing in is strictly for linking your Premium &quot;Remove Ads&quot; entitlement across devices. We do not collect, store, or sell any personal information. If you have previously purchased Premium on another device, simply sign in with the same account to restore your access.
          </p>
          <p className="text-xs text-[#94A3B8]/80">
            • Purchases on Web are secured by Paddle and synchronized across mobile devices using RevenueCat. If your status doesn&apos;t update automatically, the Restore button in the paywall will manually verify your previous transactions.
          </p>
        </div>
      </CollapsibleCard>

      {/* 4. Disclaimer Card */}
      <CollapsibleCard
        title="Disclaimer & Fair Use"
        icon={<Scale className="w-5 h-5 text-[#7098F8]" />}
        initialExpanded={false}
      >
        <div className="space-y-2 pt-1">
          <p>
            All team/league names, logos, and brands used in this application are the property of their respective owners. They are used strictly for identification and informational purposes under fair use.
          </p>
          <p>
            This application is unofficial and is not affiliated with, sponsored by, or endorsed by any football club, league, or broadcaster.
          </p>
        </div>
      </CollapsibleCard>

      {/* 5. Privacy Policy & Terms Card */}
      <CollapsibleCard
        title="Privacy Policy & Terms"
        icon={<FileText className="w-5 h-5 text-[#7098F8]" />}
        initialExpanded={false}
      >
        <div className="space-y-4 pt-1">
          <p>
            Football Highlights & Clips uses official YouTube API Services and player embeds. By accessing or using this service, you agree to be bound by the YouTube Terms of Service and Google Privacy Policy. Review our full legal documentation below:
          </p>
          <div className="flex flex-wrap items-center gap-3">
            <Link
              href="/privacy"
              className="px-4 py-2 rounded-xl bg-[#1E2227] hover:bg-[#252B32] border border-[#334155] text-white text-xs font-bold transition-all hover:border-[#7098F8]/50"
            >
              Privacy Policy & Cookies
            </Link>
            <Link
              href="/terms"
              className="px-4 py-2 rounded-xl bg-[#1E2227] hover:bg-[#252B32] border border-[#334155] text-white text-xs font-bold transition-all hover:border-[#7098F8]/50"
            >
              Terms of Service
            </Link>
          </div>
        </div>
      </CollapsibleCard>
    </div>
  );
}
