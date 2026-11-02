'use client';

import React, { useEffect, useRef } from 'react';
import * as echarts from 'echarts/core';
import { LineChart } from 'echarts/charts';
import {
  GridComponent,
  TooltipComponent,
  VisualMapComponent,
} from 'echarts/components';
import { CanvasRenderer } from 'echarts/renderers';

echarts.use([LineChart, GridComponent, TooltipComponent, VisualMapComponent, CanvasRenderer]);

interface MatchMomentumChartProps {
  homeTeam: string;
  awayTeam: string;
  homeScore?: number | null;
  awayScore?: number | null;
}

export default function MatchMomentumChart({
  homeTeam,
  awayTeam,
  homeScore = 0,
  awayScore = 0,
}: MatchMomentumChartProps) {
  const chartRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (!chartRef.current) return;

    const chartInstance = echarts.init(chartRef.current, 'dark', {
      renderer: 'canvas',
    });

    // Generate minute-by-minute momentum simulation (1' to 90')
    const minutes = Array.from({ length: 90 }, (_, i) => `${i + 1}'`);
    const momentumValues = Array.from({ length: 90 }, (_, i) => {
      const base = Math.sin(i / 6) * 40 + Math.cos(i / 3) * 20;
      const scoreBias = ((homeScore ?? 0) - (awayScore ?? 0)) * 8;
      return Math.round(Math.max(-75, Math.min(75, base + scoreBias)));
    });

    const option: echarts.EChartsCoreOption = {
      backgroundColor: 'transparent',
      tooltip: {
        trigger: 'axis',
        formatter: (params: any) => {
          const item = params[0];
          const val = Number(item.value);
          const dominantTeam = val >= 0 ? homeTeam : awayTeam;
          return `<div class="p-1 font-mono text-xs">
            <span class="font-bold text-gray-400">${item.name}</span><br/>
            <span class="${val >= 0 ? 'text-blue-400' : 'text-rose-400'} font-bold">
              ${dominantTeam} Dominance (+${Math.abs(val)}%)
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
          { gt: 0, color: '#3B82F6' },
          { lte: 0, color: '#F43F5E' },
        ],
      },
      series: [
        {
          name: 'Momentum',
          type: 'line',
          smooth: true,
          data: momentumValues,
          areaStyle: {
            opacity: 0.35,
          },
          lineStyle: { width: 2 },
          showSymbol: false,
        },
      ],
    };

    chartInstance.setOption(option);

    function handleResize() {
      chartInstance.resize();
    }
    window.addEventListener('resize', handleResize);

    return () => {
      window.removeEventListener('resize', handleResize);
      chartInstance.dispose();
    };
  }, [homeTeam, awayTeam, homeScore, awayScore]);

  return (
    <div className="bg-[#161B22]/60 border border-white/10 rounded-2xl p-4 shadow-xl">
      <div className="flex items-center justify-between mb-2">
        <span className="text-xs font-bold text-blue-400 truncate max-w-[140px]">
          ▲ {homeTeam}
        </span>
        <span className="text-[10px] font-mono font-bold tracking-wider text-gray-400 uppercase">
          Match Dominance Wave
        </span>
        <span className="text-xs font-bold text-rose-400 truncate max-w-[140px] text-right">
          {awayTeam} ▼
        </span>
      </div>
      <div ref={chartRef} className="w-full h-44" />
    </div>
  );
}
