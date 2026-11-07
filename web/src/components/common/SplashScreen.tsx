'use client';

// On Web platforms, full-screen blocking splash screens obstruct search crawlers and AdSense reviewers.
// Return null so web content renders immediately without viewport occlusion.
export default function SplashScreen() {
  return null;
}
