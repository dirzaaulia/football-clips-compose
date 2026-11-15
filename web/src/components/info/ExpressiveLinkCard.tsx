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
      className="flex items-center justify-between p-3.5 rounded-xl bg-[#121316] border border-white/10 hover:border-[#DFFF00]/40 hover:bg-[#1A1C22] transition-all group"
    >
      <div className="flex items-center gap-3">
        <div className="w-8 h-8 rounded-lg bg-white/5 border border-white/10 flex items-center justify-center shrink-0">
          {icon}
        </div>
        <div>
          <h4 className="text-xs sm:text-sm font-bold text-white group-hover:text-[#DFFF00] transition-colors font-tech">
            {title}
          </h4>
          <span className="text-[10px] text-white/40 font-tech">{subtitle}</span>
        </div>
      </div>
      <ArrowUpRight className="w-4 h-4 text-white/50 group-hover:text-[#DFFF00] group-hover:translate-x-0.5 group-hover:-translate-y-0.5 transition-transform" />
    </a>
  );
}
