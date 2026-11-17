'use client';

import React, { useEffect, useRef } from 'react';
import * as echarts from 'echarts/core';
import { LineChart } from 'echarts/charts';
import {
  GridComponent,
  TooltipComponent,
  VisualMapComponent,
  VisualMapPiecewiseComponent,
  VisualMapContinuousComponent,
} from 'echarts/components';
import { CanvasRenderer } from 'echarts/renderers';
import { MatchEvent } from '@/lib/database';

echarts.use([
  LineChart,
  GridComponent,
  TooltipComponent,
  VisualMapComponent,
  VisualMapPiecewiseComponent,
  VisualMapContinuousComponent,
  CanvasRenderer,
]);

interface MatchMomentumChartProps {
  homeTeam: string;
  awayTeam: string;
  homeScore?: number | null;
  awayScore?: number | null;
  homeColor?: string;
  awayColor?: string;
  events?: MatchEvent[] | null;
  possHome?: number;
}

function parseMinuteNumber(clock?: string): number {
  if (!clock) return 45;
  const clean = clock.replace(/'/g, '').trim();
  if (clean.includes('+')) {
    const parts = clean.split('+');
    return Math.min(90, (parseInt(parts[0], 10) || 45) + (parseInt(parts[1], 10) || 1));
  }
  return Math.min(Math.max(parseInt(clean, 10) || 45, 1), 90);
}

export default function MatchMomentumChart({
  homeTeam,
  awayTeam,
  homeScore = 0,
  awayScore = 0,
  homeColor = '#DFFF00',
  awayColor = '#38BDF8',
  events,
  possHome = 50,
}: MatchMomentumChartProps) {
  const chartRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const container = chartRef.current;
    if (!container) return;

    let chartInstance: echarts.ECharts | null = null;
    try {
      chartInstance =
        echarts.getInstanceByDom(container) ||
        echarts.init(container, undefined, { renderer: 'canvas' });
    } catch {
      return;
    }

    const minutes = Array.from({ length: 90 }, (_, i) => `${i + 1}'`);
    const basePossBias = (possHome - 50) * 0.65;
    const scoreBias = ((homeScore ?? 0) - (awayScore ?? 0)) * 5;

    // Build impulse map from real match events
    const impulses = new Array<number>(90).fill(0);
    for (const ev of events || []) {
      const mIdx = Math.min(89, Math.max(0, parseMinuteNumber(ev.clock) - 1));
      const dir = ev.side === 'away' ? -1 : 1;
      const t = (ev.type || ev.type_text || '').toLowerCase();
      const weight = ev.scoring_play || t.includes('goal') ? 38 : t.includes('shot') ? 22 : t.includes('corner') ? 16 : 10;
      for (let offset = -3; offset <= 3; offset++) {
        const idx = mIdx + offset;
        if (idx >= 0 && idx < 90) {
          impulses[idx] += dir * weight * (1 - Math.abs(offset) / 4);
        }
      }
    }

    const momentumValues = Array.from({ length: 90 }, (_, i) => {
      const rhythm = Math.sin((i + 1) / 5.5) * 16 + Math.cos((i + 1) / 3.2) * 10;
      const val = basePossBias + scoreBias + rhythm + impulses[i];
      return Math.round(Math.max(-85, Math.min(85, val)));
    });

    const option: echarts.EChartsCoreOption = {
      backgroundColor: 'transparent',
      tooltip: {
        trigger: 'axis',
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        formatter: (params: any) => {
          const item = Array.isArray(params) ? params[0] : params;
          if (!item) return '';
          const val = Number(item.value || 0);
          const dominantTeam = val >= 0 ? homeTeam : awayTeam;
          const dominantColor = val >= 0 ? homeColor : awayColor;
          return `<div class="p-1 font-mono text-xs">
            <span class="font-bold text-gray-400">${item.name}</span><br/>
            <span style="color: ${dominantColor}; font-weight: 800;">
              ${dominantTeam} Pressure (+${Math.abs(val)}%)
            </span>
          </div>`;
        },
      },
      grid: {
        left: '2%',
        right: '2%',
        top: '15%',
        bottom: '10%',
        containLabel: true,
      },
      xAxis: {
        type: 'category',
        data: minutes,
        axisLine: { lineStyle: { color: 'rgba(255,255,255,0.1)' } },
        axisLabel: { color: '#94A3B8', interval: 14, fontSize: 10 },
        splitLine: { show: false },
      },
      yAxis: {
        type: 'value',
        min: -100,
        max: 100,
        axisLabel: { show: false },
        splitLine: {
          lineStyle: { color: 'rgba(255,255,255,0.05)', type: 'dashed' },
        },
      },
      visualMap: {
        show: false,
        dimension: 1,
        pieces: [
          { gt: 0, color: homeColor },
          { lte: 0, color: awayColor },
        ],
      },
      series: [
        {
          name: 'Momentum',
          type: 'line',
          smooth: true,
          data: momentumValues,
          areaStyle: { opacity: 0.35 },
          lineStyle: { width: 2 },
          showSymbol: false,
        },
      ],
    };

    try {
      chartInstance?.setOption(option);
    } catch {
      // Graceful fallback if DOM or canvas is disposed
    }

    const handleResize = () => {
      if (chartInstance && !chartInstance.isDisposed()) {
        chartInstance.resize();
      }
    };
    window.addEventListener('resize', handleResize);

    return () => {
      window.removeEventListener('resize', handleResize);
      if (chartInstance && !chartInstance.isDisposed()) {
        chartInstance.dispose();
      }
    };
  }, [homeTeam, awayTeam, homeScore, awayScore, homeColor, awayColor, events, possHome]);

  return (
    <div className="bg-[#0A0A0C] border border-white/10 rounded-xl p-4 shadow-inner">
      <div className="flex items-center justify-between mb-2">
        <span className="text-xs font-bold truncate max-w-[140px]" style={{ color: homeColor }}>
          ▲ {homeTeam}
        </span>
        <span className="text-[10px] font-mono font-bold tracking-wider text-white/50 uppercase">
          Match Pressure &amp; Tempo
        </span>
        <span className="text-xs font-bold truncate max-w-[140px] text-right" style={{ color: awayColor }}>
          {awayTeam} ▼
        </span>
      </div>
      <div ref={chartRef} className="w-full h-40" />
    </div>
  );
}
