'use client';

import React from 'react';
import Script from 'next/script';
import { usePathname } from 'next/navigation';

export default function AdSenseScript() {
  const pathname = usePathname();
  const isTelemetry = pathname?.startsWith('/telemetry') ?? false;

  // Never load Google AdSense on internal operations & telemetry dashboard
  if (isTelemetry) {
    return null;
  }

  const adsenseClientId = process.env.NEXT_PUBLIC_ADSENSE_CLIENT_ID || 'ca-pub-6717632447198427';

  return (
    <Script
      id="google-adsense"
      strategy="afterInteractive"
      src={`https://pagead2.googlesyndication.com/pagead/js/adsbygoogle.js?client=${adsenseClientId}`}
      crossOrigin="anonymous"
    />
  );
}
