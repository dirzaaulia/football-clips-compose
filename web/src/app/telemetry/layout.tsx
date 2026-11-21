import type { Metadata } from 'next';

export const metadata: Metadata = {
  title: 'Telemetry & Operations Dashboard | Football Highlights & Clips',
  robots: {
    index: false,
    follow: false,
  },
};

export default function TelemetryLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return children;
}
