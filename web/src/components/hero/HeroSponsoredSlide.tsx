'use client';

import React from 'react';

// During Google AdSense site approval review, manual placeholder ad containers are suppressed
// to prevent "Valuable inventory: No content / Site under construction" rejections.
// The verified Google AdSense script is active in layout.tsx head to verify ownership and Auto Ads.
const SUPPRESS_MANUAL_ADS_FOR_REVIEW = true;

export default function HeroSponsoredSlide() {
  if (SUPPRESS_MANUAL_ADS_FOR_REVIEW) {
    return null;
  }

  return null;
}
