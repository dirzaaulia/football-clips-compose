'use client';

import React, { useState, useMemo } from 'react';
import { Smartphone, Globe, Search, ShieldCheck, MapPin } from 'lucide-react';
import { AppSessionItem, UserAnalyticsEvent } from '../../types';

interface DeviceLocationTableProps {
  appSessions: AppSessionItem[];
  events: UserAnalyticsEvent[];
  myDeviceId?: string | null;
}

const COUNTRY_FLAGS: Record<string, string> = {
  ID: '🇮🇩', GB: '🇬🇧', US: '🇺🇸', DE: '🇩🇪', FR: '🇫🇷',
  ES: '🇪🇸', IT: '🇮🇹', NL: '🇳🇱', BR: '🇧🇷', AR: '🇦🇷',
  TR: '🇹🇷', GR: '🇬🇷', SA: '🇸🇦', AE: '🇦🇪', SG: '🇸🇬',
  MY: '🇲🇾', TH: '🇹🇭', VN: '🇻🇳', JP: '🇯🇵', KR: '🇰🇷',
};

function formatWib(isoStr?: string | null): string {
  if (!isoStr) return '—';
  try {
    const d = new Date(isoStr);
    return d.toLocaleString('en-GB', {
      timeZone: 'Asia/Jakarta',
      day: '2-digit',
      month: 'short',
      hour: '2-digit',
      minute: '2-digit',
    }) + ' WIB';
  } catch {
    return isoStr;
  }
}

export function DeviceLocationTable({
  appSessions,
  events,
  myDeviceId,
}: DeviceLocationTableProps) {
  const [searchTerm, setSearchTerm] = useState('');

  const rows = useMemo(() => {
    const deviceMap = new Map<string, {
      deviceId: string;
      platform: 'android' | 'web';
      deviceName: string;
      country?: string | null;
      lastActive: string;
      version: string;
      isMe: boolean;
      sessionCount: number;
    }>();

    // 1. Process Android App Sessions
    for (const s of appSessions) {
      const did = s.device_id;
      const isMe = Boolean(myDeviceId && did === myDeviceId);
      const existing = deviceMap.get(did);
      const count = (existing?.sessionCount || 0) + 1;
      deviceMap.set(did, {
        deviceId: did,
        platform: 'android',
        deviceName: s.device_name || existing?.deviceName || 'Android Device',
        country: s.country || existing?.country,
        lastActive: s.last_active || s.created_at,
        version: s.app_version,
        isMe,
        sessionCount: count,
      });
    }

    // 2. Process Web Events
    for (const e of events) {
      if (e.platform !== 'web') continue;
      const did = e.device_id || `web-anon-${e.id}`;
      const isMe = Boolean(myDeviceId && did === myDeviceId);
      const existing = deviceMap.get(did);
      const count = (existing?.sessionCount || 0) + 1;
      deviceMap.set(did, {
        deviceId: did,
        platform: 'web',
        deviceName: e.device_name || existing?.deviceName || 'Web Browser',
        country: e.country || existing?.country,
        lastActive: e.created_at,
        version: 'Web Portal',
        isMe,
        sessionCount: count,
      });
    }

    return Array.from(deviceMap.values()).sort((a, b) => {
      if (a.isMe && !b.isMe) return -1;
      if (!a.isMe && b.isMe) return 1;
      return new Date(b.lastActive).getTime() - new Date(a.lastActive).getTime();
    });
  }, [appSessions, events, myDeviceId]);

  const filteredRows = useMemo(() => {
    if (!searchTerm.trim()) return rows;
    const term = searchTerm.toLowerCase();
    return rows.filter((r) =>
      r.deviceName.toLowerCase().includes(term) ||
      (r.country || '').toLowerCase().includes(term) ||
      r.deviceId.toLowerCase().includes(term) ||
      r.version.toLowerCase().includes(term)
    );
  }, [rows, searchTerm]);

  return (
    <div className="bg-slate-900 border border-slate-800 rounded-2xl p-4 sm:p-5 space-y-4">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div className="space-y-0.5">
          <div className="flex items-center gap-2">
            <MapPin className="w-4 h-4 text-cyan-400" />
            <h4 className="text-xs font-bold font-mono text-slate-100 uppercase tracking-wider">
              Connected Devices & Geographic Location
            </h4>
          </div>
          <p className="text-[11px] text-slate-400 font-mono">
            {filteredRows.length} distinct devices tracked across Google Play Android & Web
          </p>
        </div>

        <div className="relative w-full sm:w-64">
          <Search className="w-3.5 h-3.5 text-slate-400 absolute left-2.5 top-1/2 -translate-y-1/2" />
          <input
            type="text"
            placeholder="Search device, country, ID..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="w-full bg-slate-950 border border-slate-800 rounded-lg pl-8 pr-3 py-1.5 text-xs font-mono text-slate-200 placeholder-slate-500 focus:outline-none focus:border-cyan-500"
          />
        </div>
      </div>

      <div className="overflow-x-auto border border-slate-800/80 rounded-xl">
        <table className="w-full text-left text-xs font-mono">
          <thead className="bg-slate-950/60 text-slate-400 border-b border-slate-800 text-[11px] uppercase tracking-wider">
            <tr>
              <th className="py-2.5 px-3">Device Model</th>
              <th className="py-2.5 px-3">Location</th>
              <th className="py-2.5 px-3">Platform</th>
              <th className="py-2.5 px-3">Version</th>
              <th className="py-2.5 px-3 text-right">Last Active (WIB)</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-800/60">
            {filteredRows.length === 0 ? (
              <tr>
                <td colSpan={5} className="py-8 text-center text-slate-500">
                  No matching devices found in buffer.
                </td>
              </tr>
            ) : (
              filteredRows.slice(0, 30).map((r) => {
                const flag = r.country ? (COUNTRY_FLAGS[r.country.toUpperCase()] || '🌐') : '🌐';
                return (
                  <tr key={r.deviceId} className={`hover:bg-slate-800/30 transition ${r.isMe ? 'bg-cyan-950/20' : ''}`}>
                    <td className="py-2 px-3">
                      <div className="flex items-center gap-1.5">
                        <span className="font-semibold text-slate-100">{r.deviceName}</span>
                        {r.isMe && (
                          <span className="inline-flex items-center gap-0.5 px-1.5 py-0.2 rounded text-[10px] bg-cyan-500/20 text-cyan-300 border border-cyan-500/40">
                            <ShieldCheck className="w-2.5 h-2.5" /> YOU
                          </span>
                        )}
                      </div>
                      <span className="text-[10px] text-slate-500 block truncate max-w-[180px]">{r.deviceId}</span>
                    </td>
                    <td className="py-2 px-3 text-slate-300">
                      <span className="inline-flex items-center gap-1">
                        <span>{flag}</span>
                        <span>{r.country || 'Global'}</span>
                      </span>
                    </td>
                    <td className="py-2 px-3">
                      <span className={`inline-flex items-center gap-1 text-[11px] font-bold ${r.platform === 'android' ? 'text-cyan-400' : 'text-indigo-400'}`}>
                        {r.platform === 'android' ? <Smartphone className="w-3 h-3" /> : <Globe className="w-3 h-3" />}
                        {r.platform === 'android' ? 'Android' : 'Web'}
                      </span>
                    </td>
                    <td className="py-2 px-3 text-slate-400 text-[11px]">
                      {r.version}
                    </td>
                    <td className="py-2 px-3 text-right text-slate-400 text-[11px]">
                      {formatWib(r.lastActive)}
                    </td>
                  </tr>
                );
              })
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
