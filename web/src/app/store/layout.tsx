import type { Metadata } from 'next';

export const metadata: Metadata = {
  title: 'Official Club Store & Authentic Kits | Football Highlights & Clips',
  robots: {
    index: false,
    follow: false,
  },
};

export default function StoreLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return children;
}
