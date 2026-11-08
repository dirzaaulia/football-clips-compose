'use client';

import React, { useState } from 'react';
import { ExternalLink, ChevronDown, ChevronRight } from 'lucide-react';
import { ScannedVideo } from '../types';
import { DirectCandidatesAccordionItem } from './CandidateDirectGroup';

interface CandidateGroupListProps {
  scannedUploads?: Record<string, ScannedVideo[]>;
  directCandidates: string[];
}

function VideoItemRow({ vid }: { vid: ScannedVideo }) {
  return (
    <div className="flex items-center justify-between gap-2 p-1.5 rounded bg-slate-950/60 hover:bg-slate-800/50 text-[11px] border border-slate-800/40">
      <span className="truncate text-slate-300 font-sans" title={vid.title}>
        {vid.title}
      </span>
      <a
        href={`https://youtu.be/${vid.id}`}
        target="_blank"
        rel="noopener noreferrer"
        className="inline-flex items-center gap-1 px-2 py-0.5 rounded bg-slate-800 text-blue-400 hover:text-blue-300 text-[10px] font-bold shrink-0 border border-slate-700 font-mono transition"
      >
        <span>Watch</span>
        <ExternalLink className="w-2.5 h-2.5" />
      </a>
    </div>
  );
}

interface ChannelHeaderProps {
  channel: string;
  count: number;
  sourceType: 'rss' | 'api_search' | 'api_playlist';
  isExpanded: boolean;
  onToggle: () => void;
}

function ChannelAccordionHeader({
  channel,
  count,
  sourceType,
  isExpanded,
  onToggle,
}: ChannelHeaderProps) {
  const badgeClass =
    sourceType === 'rss'
      ? 'bg-emerald-500/15 text-emerald-400 border-emerald-500/30'
      : sourceType === 'api_search'
        ? 'bg-purple-500/15 text-purple-300 border-purple-500/30'
        : 'bg-blue-500/15 text-blue-400 border-blue-500/30';
  const badgeLabel =
    sourceType === 'rss'
      ? 'RSS 0-quota'
      : sourceType === 'api_search'
        ? 'API Search (100 quota)'
        : 'API Data';

  return (
    <button
      type="button"
      onClick={onToggle}
      className="w-full flex items-center justify-between gap-2 p-2 hover:bg-slate-800/50 transition cursor-pointer text-left"
    >
      <div className="flex items-center gap-1.5 font-sans font-medium text-slate-200 min-w-0">
        {isExpanded ? (
          <ChevronDown className="w-3.5 h-3.5 text-slate-400 shrink-0" />
        ) : (
          <ChevronRight className="w-3.5 h-3.5 text-slate-400 shrink-0" />
        )}
        <span className="font-mono text-blue-400 font-semibold text-[11px] truncate">
          {channel}
        </span>
        <span className="text-slate-500 font-normal text-[11px] shrink-0">
          ({count} {count === 1 ? 'upload' : 'uploads'})
        </span>
      </div>
      <div className="flex items-center gap-1.5 shrink-0">
        <span className={`px-1.5 py-0.5 rounded text-[9px] font-bold border ${badgeClass}`}>
          {badgeLabel}
        </span>
        <span className="text-[10px] font-mono text-slate-500">
          {isExpanded ? 'Hide' : 'Show'}
        </span>
      </div>
    </button>
  );
}

function ChannelAccordionItem({
  channel,
  videos,
  isExpanded,
  onToggle,
}: {
  channel: string;
  videos: ScannedVideo[];
  isExpanded: boolean;
  onToggle: () => void;
}) {
  const firstSrc = String(videos[0]?.source || '');
  const sourceType: 'rss' | 'api_search' | 'api_playlist' =
    channel.startsWith('API ') || firstSrc === 'api_search'
      ? 'api_search'
      : firstSrc === 'rss' || !firstSrc
        ? 'rss'
        : 'api_playlist';

  return (
    <div className="rounded-lg border border-slate-800/60 bg-slate-950/40 overflow-hidden">
      <ChannelAccordionHeader
        channel={channel}
        count={videos.length}
        sourceType={sourceType}
        isExpanded={isExpanded}
        onToggle={onToggle}
      />
      {isExpanded && (
        <div className="p-2 pt-0 space-y-1 border-t border-slate-800/40 mt-1">
          {videos.map((vid) => (
            <VideoItemRow key={vid.id} vid={vid} />
          ))}
        </div>
      )}
    </div>
  );
}

export function CandidateGroupList({
  scannedUploads,
  directCandidates,
}: CandidateGroupListProps) {
  const scannedEntries = Object.entries(scannedUploads || {});
  const totalScanned = scannedEntries.reduce((sum, [, vids]) => sum + vids.length, 0);
  const totalCount = totalScanned + directCandidates.length;

  const [expandedChannels, setExpandedChannels] = useState<Set<string>>(new Set());
  const [directExpanded, setDirectExpanded] = useState<boolean>(false);

  if (totalCount === 0) {
    return <p className="text-slate-500 italic pl-6 text-xs">Zero preliminary uploads detected</p>;
  }

  const allChannelsExpanded = scannedEntries.length > 0 && expandedChannels.size === scannedEntries.length;

  const handleToggleChannel = (channel: string) => {
    setExpandedChannels((prev) => {
      const next = new Set(prev);
      if (next.has(channel)) {
        next.delete(channel);
      } else {
        next.add(channel);
      }
      return next;
    });
  };

  const handleToggleAll = () => {
    if (allChannelsExpanded) {
      setExpandedChannels(new Set());
      setDirectExpanded(false);
    } else {
      setExpandedChannels(new Set(scannedEntries.map(([ch]) => ch)));
      if (directCandidates.length > 0) setDirectExpanded(true);
    }
  };

  return (
    <div className="bg-slate-900/90 rounded-lg p-2.5 text-slate-300 font-mono space-y-2 border border-slate-800 max-h-96 overflow-y-auto">
      {scannedEntries.length > 1 && (
        <div className="flex items-center justify-between px-1 pb-1 border-b border-slate-800/40">
          <span className="text-[10px] text-slate-400">
            {scannedEntries.length} Channels Scanned
          </span>
          <button
            type="button"
            onClick={handleToggleAll}
            className="text-[10px] text-blue-400 hover:text-blue-300 font-sans font-semibold underline decoration-dotted transition cursor-pointer"
          >
            {allChannelsExpanded ? 'Collapse all' : 'Expand all'}
          </button>
        </div>
      )}

      {scannedEntries.map(([channel, videos]) => (
        <ChannelAccordionItem
          key={channel}
          channel={channel}
          videos={videos}
          isExpanded={expandedChannels.has(channel)}
          onToggle={() => handleToggleChannel(channel)}
        />
      ))}

      {directCandidates.length > 0 && (
        <DirectCandidatesAccordionItem
          candidates={directCandidates}
          isExpanded={directExpanded}
          onToggle={() => setDirectExpanded((prev) => !prev)}
        />
      )}
    </div>
  );
}
