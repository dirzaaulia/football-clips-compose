import React from 'react';
import Link from 'next/link';

export const metadata = {
  title: 'Terms and Conditions - Football Highlights & Clips',
  description: 'Terms and Conditions of Service for Football Highlights & Clips (Football Highlights & Clips / FC).'
};

export default function TermsPage() {
  return (
    <main className="max-w-4xl mx-auto px-4 sm:px-6 py-12 text-zinc-300 space-y-8">
      <header className="border-b border-zinc-800 pb-6 space-y-2">
        <span className="inline-block px-2.5 py-1 rounded-full text-xs font-semibold bg-sky-500/10 text-sky-400">
          Agreement
        </span>
        <h1 className="text-3xl font-black text-white tracking-tight">Terms and Conditions</h1>
        <p className="text-xs text-zinc-400">
          Last updated: September 5, 2026 • Effective date: September 5, 2026
        </p>
      </header>

      <section className="space-y-4 text-sm leading-relaxed">
        <p>
          Welcome to <strong className="text-white">Football Highlights &amp; Clips</strong> (also referred to as{' '}
          <strong className="text-white">Football Highlights &amp; Clips</strong> or <strong className="text-white">FC</strong>, &quot;we&quot;, &quot;us&quot;, or &quot;our&quot;),
          developed by <strong className="text-white">Dirza Aulia</strong>. These Terms and Conditions (&quot;Terms&quot;) govern your access to and use of the
          Football Highlights &amp; Clips mobile application, website (
          <a href="https://fc.dirzaaulia.com" className="text-sky-400 hover:underline">
            fc.dirzaaulia.com
          </a>
          ), and related services.
        </p>
        <p>
          By downloading, accessing, or using Football Highlights &amp; Clips, you agree to be bound by these Terms. If you do not agree to these Terms, please do not use our services.
        </p>
      </section>

      <section className="space-y-3">
        <h2 className="text-lg font-bold text-white border-l-2 border-sky-400 pl-3">
          1. Service Overview &amp; Intellectual Property
        </h2>
        <p className="text-sm leading-relaxed">
          Football Highlights &amp; Clips is a football companion platform providing match highlights, scores, and fixtures across major leagues:
        </p>
        <ul className="list-disc list-inside space-y-2 text-sm pl-2">
          <li>
            <strong className="text-white">Official Video Embeds:</strong> Football Highlights &amp; Clips does <em>not</em> host, upload, or broadcast pirated video media.
            All match clips and video highlights are embedded directly from official broadcaster and club channels via the standard{' '}
            <strong className="text-white">YouTube Player API</strong> and <strong className="text-white">YouTube API Services</strong>.
          </li>
          <li>
            <strong className="text-white">Third-Party Content Rights &amp; YouTube Terms of Service:</strong> All video content, team logos, trademarks, and broadcaster assets
            belong entirely to their respective copyright holders. Use of embedded videos is governed by the{' '}
            <a href="https://www.youtube.com/t/terms" target="_blank" rel="noopener noreferrer" className="text-sky-400 hover:underline">
              YouTube Terms of Service
            </a>
            . By downloading, accessing, or viewing video highlights within Football Highlights &amp; Clips, you acknowledge and agree to be bound by the{' '}
            <a href="https://www.youtube.com/t/terms" target="_blank" rel="noopener noreferrer" className="text-sky-400 hover:underline">
              YouTube Terms of Service
            </a>{' '}
            and the{' '}
            <a href="https://policies.google.com/privacy" target="_blank" rel="noopener noreferrer" className="text-sky-400 hover:underline">
              Google Privacy Policy
            </a>
            .
          </li>
          <li>
            <strong className="text-white">App Intellectual Property:</strong> The Football Highlights &amp; Clips application, user interface design, logo, and source code
            are the intellectual property of Dirza Aulia.
          </li>
        </ul>
      </section>

      <section className="space-y-3">
        <h2 className="text-lg font-bold text-white border-l-2 border-sky-400 pl-3">
          2. In-App Purchases &amp; &quot;Remove Ads&quot;
        </h2>
        <p className="text-sm leading-relaxed">
          Football Highlights &amp; Clips is free to use with optional in-app digital purchases:
        </p>
        <ul className="list-disc list-inside space-y-2 text-sm pl-2">
          <li>
            <strong className="text-white">Remove Ads Feature:</strong> Users can optionally purchase a lifetime or recurring &quot;Remove Ads&quot; entitlement to enjoy an ad-free experience.
          </li>
          <li>
            <strong className="text-white">Billing &amp; Processing:</strong> On Android, purchases are processed securely through Google Play Billing and validated via RevenueCat. On the Web, purchases are processed via our authorized web payment provider.
          </li>
          <li>
            <strong className="text-white">Account Linking:</strong> You may sign in with your Google Account to restore and synchronize your purchase across supported devices.
          </li>
          <li>
            <strong className="text-white">Refunds:</strong> Refund requests for in-app purchases on Android are subject to Google Play&apos;s standard refund policies. You can submit refund requests directly via the Google Play Store.
          </li>
        </ul>
      </section>

      <section className="space-y-3">
        <h2 className="text-lg font-bold text-white border-l-2 border-sky-400 pl-3">
          3. User Conduct &amp; Acceptable Use
        </h2>
        <p className="text-sm leading-relaxed">
          When using Football Highlights &amp; Clips (FC), you agree not to:
        </p>
        <ul className="list-disc list-inside space-y-1.5 text-sm pl-2">
          <li>Reverse-engineer, decompile, or attempt to extract the source code of the application.</li>
          <li>Circumvent or tamper with digital rights management, advertising systems, or entitlement checks.</li>
          <li>Use automated bots, scrapers, or excessive queries that could disrupt or burden our backend infrastructure.</li>
        </ul>
      </section>

      <section className="space-y-3">
        <h2 className="text-lg font-bold text-white border-l-2 border-sky-400 pl-3">
          4. Advertisements
        </h2>
        <p className="text-sm leading-relaxed">
          To support ongoing maintenance and server costs, Football Highlights &amp; Clips displays advertisements served by Google AdMob (on mobile) and Google AdSense (on web) for non-premium users. We are not responsible for the content of third-party advertisements or external links provided in ads.
        </p>
      </section>

      <section className="space-y-3">
        <h2 className="text-lg font-bold text-white border-l-2 border-sky-400 pl-3">
          5. Disclaimer of Warranties
        </h2>
        <p className="text-sm leading-relaxed">
          Football Highlights &amp; Clips is provided on an <strong className="text-white">&quot;AS IS&quot;</strong> and <strong className="text-white">&quot;AS AVAILABLE&quot;</strong> basis without warranties of any kind, whether express or implied. While we strive to maintain accurate live scores and up-to-date highlight links, we do not warrant that:
        </p>
        <ul className="list-disc list-inside space-y-1.5 text-sm pl-2">
          <li>The service will be uninterrupted, error-free, or entirely secure.</li>
          <li>Highlight videos hosted on YouTube will remain available indefinitely (availability depends on rights holders).</li>
        </ul>
      </section>

      <section className="space-y-3">
        <h2 className="text-lg font-bold text-white border-l-2 border-sky-400 pl-3">
          6. Limitation of Liability
        </h2>
        <p className="text-sm leading-relaxed">
          To the maximum extent permitted by applicable law, Dirza Aulia and Football Highlights &amp; Clips shall not be liable for any indirect, incidental, special, consequential, or punitive damages arising out of your access to or use of the application.
        </p>
      </section>

      <section className="space-y-3">
        <h2 className="text-lg font-bold text-white border-l-2 border-sky-400 pl-3">
          7. Termination
        </h2>
        <p className="text-sm leading-relaxed">
          We reserve the right to suspend or terminate your access to Football Highlights &amp; Clips at our sole discretion, without prior notice, for conduct that violates these Terms or harms other users or the service.
        </p>
      </section>

      <section className="space-y-3">
        <h2 className="text-lg font-bold text-white border-l-2 border-sky-400 pl-3">
          8. Changes to These Terms
        </h2>
        <p className="text-sm leading-relaxed">
          We reserve the right to modify or replace these Terms at any time. If revisions are material, we will update the &quot;Last updated&quot; date at the top of this page. Your continued use of Football Highlights &amp; Clips following any changes signifies your acceptance of the updated Terms.
        </p>
      </section>

      <section className="space-y-3 border-t border-zinc-800 pt-6">
        <h2 className="text-lg font-bold text-white">9. Contact Information</h2>
        <p className="text-sm leading-relaxed">
          If you have any questions or feedback regarding these Terms, please contact us:
        </p>
        <ul className="list-none space-y-1 text-sm text-zinc-300">
          <li><span className="font-semibold text-white">Developer:</span> Dirza Aulia</li>
          <li>
            <span className="font-semibold text-white">Email:</span>{' '}
            <a href="mailto:dirzaaulia11@gmail.com" className="text-sky-400 hover:underline">
              dirzaaulia11@gmail.com
            </a>
          </li>
          <li>
            <span className="font-semibold text-white">Website:</span>{' '}
            <Link href="/" className="text-sky-400 hover:underline">
              fc.dirzaaulia.com
            </Link>
          </li>
        </ul>
      </section>
    </main>
  );
}
