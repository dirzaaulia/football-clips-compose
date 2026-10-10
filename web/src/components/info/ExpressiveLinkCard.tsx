import React from 'react';
import { ArrowUpRight } from 'lucide-react';

interface ExpressiveLinkCardProps {
  title: string;
  subtitle: string;
  icon: React.ReactNode;
  href: string;
}

export default function ExpressiveLinkCard({
  title,
  subtitle,
  icon,
  href,
}: ExpressiveLinkCardProps) {
  return (
    <a
      href={href}
      target="_blank"
      rel="noopener noreferrer"
      className="flex items-center justify-between p-3.5 rounded-xl bg-[#1E2227] border border-[#334155]/60 hover:border-[#7098F8]/50 hover:bg-[#252B32] transition-all group"
    >
      <div className="flex items-center gap-3">
        <div className="w-8 h-8 rounded-lg bg-[#7098F8]/15 border border-[#7098F8]/30 flex items-center justify-center shrink-0">
          {icon}
        </div>
        <div>
          <h4 className="text-xs sm:text-sm font-bold text-white group-hover:text-[#7098F8] transition-colors">
            {title}
          </h4>
          <span className="text-[10px] text-[#94A3B8]">{subtitle}</span>
        </div>
      </div>
      <ArrowUpRight className="w-4 h-4 text-[#7098F8] group-hover:translate-x-0.5 group-hover:-translate-y-0.5 transition-transform" />
    </a>
  );
}
