import React from 'react';
import Link from 'next/link';

export const metadata = {
  title: 'Privacy Policy - Football Highlights & Clips',
  description: 'Privacy Policy for Football Highlights & Clips (Football Highlights & Clips / FC).'
};

export default function PrivacyPage() {
  return (
    <main className="max-w-4xl mx-auto px-4 sm:px-6 py-12 text-zinc-300 space-y-8">
      <header className="border-b border-zinc-800 pb-6 space-y-2">
        <span className="inline-block px-2.5 py-1 rounded-full text-xs font-semibold bg-sky-500/10 text-sky-400">
          Legal Document
        </span>
        <h1 className="text-3xl font-black text-white tracking-tight">Privacy Policy</h1>
        <p className="text-xs text-zinc-400">
          Last updated: September 5, 2026 • Effective date: September 5, 2026
        </p>
      </header>

      <section className="space-y-4 text-sm leading-relaxed">
        <p>
          Welcome to <strong className="text-white">Football Highlights &amp; Clips</strong> (also referred to as{' '}
          <strong className="text-white">Football Highlights &amp; Clips</strong> or <strong className="text-white">FC</strong>, &quot;we&quot;, &quot;our&quot;, or &quot;us&quot;),
          developed by <strong className="text-white">Dirza Aulia</strong>. Football Highlights &amp; Clips is available across Android and Web platforms (
          <a href="https://fc.dirzaaulia.com" className="text-sky-400 hover:underline">
            fc.dirzaaulia.com
          </a>
          ) to provide football video highlights, match scores, and schedules.
        </p>
        <p>
          We respect your privacy and are committed to protecting your personal data. This Privacy Policy explains how our service collects, uses, and safeguards information when you use our application.
        </p>
      </section>

      <section className="space-y-3">
        <h2 className="text-lg font-bold text-white border-l-2 border-sky-400 pl-3">
          1. Information We Collect
        </h2>
        <p className="text-sm leading-relaxed">
          We aim to collect the absolute minimum amount of data required to deliver a seamless experience across your devices:
        </p>
        <ul className="list-disc list-inside space-y-2 text-sm pl-2">
          <li>
            <strong className="text-white">Account &amp; Sign-In Data:</strong> If you choose to sign in with Google (via Google Credential Manager or OAuth), we receive basic public profile information (such as your email address, display name, and unique user ID) provided by Google. This data is handled securely via <strong className="text-white">Supabase Auth</strong>. Signing in is strictly used to link and sync your lifetime or active Premium (&quot;Remove Ads&quot;) purchase across multiple devices.
          </li>
          <li>
            <strong className="text-white">Purchase Data:</strong> When you purchase &quot;Remove Ads&quot; via Google Play Billing or our Web platform, transaction and receipt validation data are processed securely via <strong className="text-white">RevenueCat</strong>. We do not store or process your credit card or financial details directly.
          </li>
          <li>
            <strong className="text-white">Device &amp; Usage Data:</strong> When running the app, basic non-personally identifiable diagnostic data (such as OS version, device model, and crash logs) may be gathered via <strong className="text-white">Firebase Crashlytics &amp; Analytics</strong> to help us troubleshoot errors and improve performance.
          </li>
        </ul>
      </section>

      <section className="space-y-4">
        <h2 className="text-lg font-bold text-white border-l-2 border-sky-400 pl-3">
          2. Third-Party Services &amp; Integrations
        </h2>
        <p className="text-sm leading-relaxed">
          Our application integrates with reputable third-party service providers who may collect information used to identify your device:
        </p>

        <div className="grid gap-4">
          <div className="p-4 rounded-xl bg-zinc-900/60 border border-zinc-800 space-y-2">
            <h3 className="text-base font-semibold text-white flex items-center gap-2">
              <span>📺</span> YouTube API Services
            </h3>
            <p className="text-sm leading-relaxed">
              Football Highlights &amp; Clips uses <strong className="text-white">YouTube API Services</strong> to display official match highlights and video recaps from verified broadcaster and club channels. By using Football Highlights &amp; Clips, you agree to be bound by the{' '}
              <a href="https://www.youtube.com/t/terms" target="_blank" rel="noopener noreferrer" className="text-sky-400 hover:underline">
                YouTube Terms of Service
              </a>.
            </p>
            <p className="text-sm leading-relaxed">
              Please refer to the{' '}
              <a href="https://policies.google.com/privacy" target="_blank" rel="noopener noreferrer" className="text-sky-400 hover:underline">
                Google Privacy Policy
              </a>{' '}
              for information on how Google collects, uses, and protects your data.
            </p>
            <p className="text-xs text-zinc-400 leading-relaxed">
              <strong className="text-zinc-200">Data Accessed &amp; Stored:</strong> Our application accesses public YouTube video metadata (such as video IDs, titles, channel titles, publication dates, and durations) via YouTube API Services strictly to match and display official football highlights alongside fixture details. We cache public video metadata on our secure backend database solely for performance optimization and API quota management. We do <em>not</em> download, record, or distribute raw video or audio files, nor do we access, collect, or store private YouTube user data.
            </p>
          </div>

          <div className="p-4 rounded-xl bg-zinc-900/60 border border-zinc-800 space-y-2">
            <h3 className="text-base font-semibold text-white flex items-center gap-2">
              <span>📢</span> Advertising (Google AdMob &amp; Google AdSense)
            </h3>
            <p className="text-sm leading-relaxed">
              We use Google AdMob (on mobile) and Google AdSense (on web) to display advertisements. Google may use advertising IDs and cookies to serve personalized or non-personalized ads. You can opt out of personalized ads at any time by visiting{' '}
              <a href="https://adssettings.google.com" target="_blank" rel="noopener noreferrer" className="text-sky-400 hover:underline">
                Google Ads Settings
              </a>.
            </p>
          </div>

          <div className="p-4 rounded-xl bg-zinc-900/60 border border-zinc-800 space-y-2">
            <h3 className="text-base font-semibold text-white flex items-center gap-2">
              <span>💳</span> In-App Purchases (RevenueCat &amp; Google Play)
            </h3>
            <p className="text-sm leading-relaxed">
              We use RevenueCat to manage entitlements and validate receipts for digital purchases. For more information, consult{' '}
              <a href="https://www.revenuecat.com/privacy" target="_blank" rel="noopener noreferrer" className="text-sky-400 hover:underline">
                RevenueCat&apos;s Privacy Policy
              </a>.
            </p>
          </div>

          <div className="p-4 rounded-xl bg-zinc-900/60 border border-zinc-800 space-y-2">
            <h3 className="text-base font-semibold text-white flex items-center gap-2">
              <span>🛡️</span> Backend &amp; Authentication (Supabase)
            </h3>
            <p className="text-sm leading-relaxed">
              User sessions and entitlement verification are securely powered by Supabase. Data is encrypted in transit using HTTPS/TLS. View the{' '}
              <a href="https://supabase.com/privacy" target="_blank" rel="noopener noreferrer" className="text-sky-400 hover:underline">
                Supabase Privacy Policy
              </a>.
            </p>
          </div>
        </div>
      </section>

      <section className="space-y-3">
        <h2 className="text-lg font-bold text-white border-l-2 border-sky-400 pl-3">
          3. How We Use Your Information
        </h2>
        <p className="text-sm leading-relaxed">The information we collect is strictly used to:</p>
        <ul className="list-disc list-inside space-y-1.5 text-sm pl-2">
          <li>Provide and maintain the core features of Football Highlights &amp; Clips (FC).</li>
          <li>Verify, restore, and synchronize your Premium (&quot;Remove Ads&quot;) status across devices.</li>
          <li>Monitor stability, diagnose bugs, and resolve application crashes.</li>
          <li>Serve relevant advertisements to non-premium users in accordance with Google&apos;s advertising policies.</li>
        </ul>
      </section>

      <section className="space-y-3">
        <h2 className="text-lg font-bold text-white border-l-2 border-sky-400 pl-3">
          4. Data Retention &amp; Deletion Rights
        </h2>
        <p className="text-sm leading-relaxed">
          We retain your account identifier only for as long as needed to maintain your purchase entitlements. You have the right to request the deletion of your account and associated profile data at any time.
        </p>
        <p className="text-sm leading-relaxed">
          To request data deletion, please contact us at{' '}
          <a href="mailto:dirzaaulia11@gmail.com" className="text-sky-400 hover:underline">
            dirzaaulia11@gmail.com
          </a>. Upon receiving your request, your account data will be permanently removed from our databases within 30 days.
        </p>
        <p className="text-sm leading-relaxed">
          <strong className="text-white">Revocation of Google / YouTube Permissions:</strong> In addition to our data deletion procedure, users can view, manage, and revoke this application&apos;s access or permissions at any time via the Google Security Settings page at{' '}
          <a href="https://security.google.com/settings/security/permissions" target="_blank" rel="noopener noreferrer" className="text-sky-400 hover:underline">
            https://security.google.com/settings/security/permissions
          </a>.
        </p>
      </section>

      <section className="space-y-3">
        <h2 className="text-lg font-bold text-white border-l-2 border-sky-400 pl-3">
          5. Children&apos;s Privacy
        </h2>
        <p className="text-sm leading-relaxed">
          Football Highlights &amp; Clips is not targeted at children under the age of 13. We do not knowingly collect personal identifiable information from children under 13. If you believe that a child has provided us with personal data, please contact us immediately so we can take appropriate action.
        </p>
      </section>

      <section className="space-y-3">
        <h2 className="text-lg font-bold text-white border-l-2 border-sky-400 pl-3">
          6. Changes to This Privacy Policy
        </h2>
        <p className="text-sm leading-relaxed">
          We may update our Privacy Policy from time to time. Any changes will be posted on this page with an updated &quot;Last updated&quot; date. We encourage you to review this page periodically.
        </p>
      </section>

      <section className="space-y-3 border-t border-zinc-800 pt-6">
        <h2 className="text-lg font-bold text-white">7. Contact Us</h2>
        <p className="text-sm leading-relaxed">
          If you have questions or suggestions regarding this Privacy Policy, feel free to contact us:
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
