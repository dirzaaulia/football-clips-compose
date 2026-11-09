'use client';

import React, { useMemo } from 'react';
import {
  ReactFlow,
  Background,
  Controls,
  MarkerType,
  Node,
  Edge,
  NodeProps,
  Handle,
  Position,
} from '@xyflow/react';
import '@xyflow/react/dist/style.css';

interface PipelineNodeData extends Record<string, unknown> {
  title: string;
  subtitle: string;
  badge: string;
  badgeColor: string;
  metric: string;
  filterKey: string;
}

function CustomPipelineNode({ data }: NodeProps<Node<PipelineNodeData>>) {
  return (
    <div className="bg-[#161B22] border border-white/10 hover:border-blue-500/50 transition-all rounded-xl p-3 shadow-lg min-w-[200px] cursor-pointer group">
      <Handle type="target" position={Position.Left} className="w-2 h-2 !bg-blue-500" />
      <div className="flex items-center justify-between gap-2 mb-1.5">
        <span className="text-[10px] font-mono text-gray-400 font-bold uppercase tracking-wider">
          {data.title}
        </span>
        <span
          className={`text-[9px] font-bold px-1.5 py-0.5 rounded ${data.badgeColor}`}
        >
          {data.badge}
        </span>
      </div>
      <p className="text-xs font-semibold text-white group-hover:text-blue-400 transition-colors">
        {data.subtitle}
      </p>
      <div className="mt-2 pt-2 border-t border-white/5 flex items-center justify-between text-[10px] font-mono text-gray-400">
        <span>Metric</span>
        <span className="text-emerald-400 font-bold">{data.metric}</span>
      </div>
      <Handle type="source" position={Position.Right} className="w-2 h-2 !bg-blue-500" />
    </div>
  );
}

const nodeTypes = {
  pipelineNode: CustomPipelineNode,
};

interface PipelineFlowGraphProps {
  onNodeClick?: (filterKey: string) => void;
  activeFilter?: string;
  todayQuotaUsage?: number;
}

export default function PipelineFlowGraph({
  onNodeClick,
  activeFilter,
  todayQuotaUsage = 0,
}: PipelineFlowGraphProps) {
  const nodes: Node<PipelineNodeData>[] = useMemo(
    () => [
      {
        id: '1',
        type: 'pipelineNode',
        position: { x: 20, y: 70 },
        data: {
          title: 'Stage 1',
          subtitle: 'ESPN Sports Engine',
          badge: '0 QUOTA',
          badgeColor: 'bg-emerald-500/20 text-emerald-400',
          metric: '60s sync cadence',
          filterKey: 'ESPN',
        },
      },
      {
        id: '2',
        type: 'pipelineNode',
        position: { x: 270, y: 70 },
        data: {
          title: 'Stage 2',
          subtitle: 'fc-daemon (ARM64)',
          badge: '7.5MB RAM',
          badgeColor: 'bg-emerald-500/20 text-emerald-400',
          metric: 'Sub-second · $0 cost',
          filterKey: 'GO_DAEMON',
        },
      },
      {
        id: '3',
        type: 'pipelineNode',
        position: { x: 520, y: -20 },
        data: {
          title: 'Stage 3A',
          subtitle: 'Highlights Discovery',
          badge: '4-TIER GUARD',
          badgeColor: 'bg-purple-500/20 text-purple-400',
          metric: `${todayQuotaUsage.toLocaleString()} quota units`,
          filterKey: 'YOUTUBE',
        },
      },
      {
        id: '4',
        type: 'pipelineNode',
        position: { x: 520, y: 70 },
        data: {
          title: 'Stage 3B',
          subtitle: 'WebSub Push Receiver',
          badge: '<100ms',
          badgeColor: 'bg-amber-500/20 text-amber-400',
          metric: 'Port 8094 · 0 quota',
          filterKey: 'WEBSUB',
        },
      },
      {
        id: '5',
        type: 'pipelineNode',
        position: { x: 520, y: 160 },
        data: {
          title: 'Stage 3C',
          subtitle: 'Shorts RSS Ingestion',
          badge: '0 QUOTA',
          badgeColor: 'bg-pink-500/20 text-pink-400',
          metric: '30m cadence · RSS',
          filterKey: 'SHORTS',
        },
      },
      {
        id: '6',
        type: 'pipelineNode',
        position: { x: 770, y: 25 },
        data: {
          title: 'Stage 4',
          subtitle: 'Oracle AI Gateway',
          badge: 'ARM64 AI-GW',
          badgeColor: 'bg-emerald-500/20 text-emerald-400',
          metric: 'DeepSeek / Gemini Lite',
          filterKey: 'AI',
        },
      },
      {
        id: '7',
        type: 'pipelineNode',
        position: { x: 1020, y: 70 },
        data: {
          title: 'Stage 5',
          subtitle: 'PostgreSQL 16 & Caddy',
          badge: 'SUB-15ms',
          badgeColor: 'bg-cyan-500/20 text-cyan-400',
          metric: 'Anycast CDN Purge',
          filterKey: 'POSTGRESQL',
        },
      },
      {
        id: '8',
        type: 'pipelineNode',
        position: { x: 1270, y: 70 },
        data: {
          title: 'Stage 6',
          subtitle: 'Firebase FCM Alerts',
          badge: 'TOPIC PUSH',
          badgeColor: 'bg-indigo-500/20 text-indigo-400',
          metric: 'club_{id} · $0 Cost',
          filterKey: 'FCM',
        },
      },
    ],
    [todayQuotaUsage]
  );

  const edges: Edge[] = useMemo(
    () => [
      {
        id: 'e1-2',
        source: '1',
        target: '2',
        animated: true,
        style: { stroke: '#3B82F6', strokeWidth: 2 },
        markerEnd: { type: MarkerType.ArrowClosed, color: '#3B82F6' },
      },
      {
        id: 'e2-3',
        source: '2',
        target: '3',
        animated: true,
        style: { stroke: '#8B5CF6', strokeWidth: 2 },
        markerEnd: { type: MarkerType.ArrowClosed, color: '#8B5CF6' },
      },
      {
        id: 'e2-4',
        source: '2',
        target: '4',
        animated: true,
        style: { stroke: '#F59E0B', strokeWidth: 2 },
        markerEnd: { type: MarkerType.ArrowClosed, color: '#F59E0B' },
      },
      {
        id: 'e2-5',
        source: '2',
        target: '5',
        animated: true,
        style: { stroke: '#EC4899', strokeWidth: 2 },
        markerEnd: { type: MarkerType.ArrowClosed, color: '#EC4899' },
      },
      {
        id: 'e3-6',
        source: '3',
        target: '6',
        animated: true,
        style: { stroke: '#10B981', strokeWidth: 2 },
        markerEnd: { type: MarkerType.ArrowClosed, color: '#10B981' },
      },
      {
        id: 'e4-6',
        source: '4',
        target: '6',
        animated: true,
        style: { stroke: '#10B981', strokeWidth: 2 },
        markerEnd: { type: MarkerType.ArrowClosed, color: '#10B981' },
      },
      {
        id: 'e6-7',
        source: '6',
        target: '7',
        animated: true,
        style: { stroke: '#06B6D4', strokeWidth: 2 },
        markerEnd: { type: MarkerType.ArrowClosed, color: '#06B6D4' },
      },
      {
        id: 'e5-7',
        source: '5',
        target: '7',
        animated: true,
        style: { stroke: '#06B6D4', strokeWidth: 2 },
        markerEnd: { type: MarkerType.ArrowClosed, color: '#06B6D4' },
      },
      {
        id: 'e7-8',
        source: '7',
        target: '8',
        animated: true,
        style: { stroke: '#6366F1', strokeWidth: 2 },
        markerEnd: { type: MarkerType.ArrowClosed, color: '#6366F1' },
      },
    ],
    []
  );

  return (
    <div className="w-full h-64 sm:h-72 lg:h-80 bg-[#0B0F13] border border-white/10 rounded-2xl overflow-hidden relative shadow-xl">
      <div className="absolute top-3 left-4 z-10 flex items-center gap-2">
        <span className="flex h-2 w-2 rounded-full bg-emerald-400 animate-ping" />
        <span className="text-[11px] font-mono font-bold text-gray-300">
          Live Topological Dataflow Engine (Interactive)
        </span>
      </div>
      <ReactFlow
        nodes={nodes}
        edges={edges}
        nodeTypes={nodeTypes}
        fitView
        colorMode="dark"
        nodesDraggable={false}
        nodesConnectable={false}
        onNodeClick={(_, node) => onNodeClick && onNodeClick(String(node.data?.filterKey ?? ''))}
        proOptions={{ hideAttribution: true }}
      >
        <Background color="#161B22" gap={16} size={1} />
        <Controls
          showInteractive={false}
          className="!bg-[#161B22] !border-white/10 [&>button]:!bg-[#161B22] [&>button]:!border-white/10 [&>button]:!fill-slate-200 [&>button:hover]:!bg-[#21262d] [&>button>svg]:!fill-slate-200 shadow-xl"
        />
      </ReactFlow>
    </div>
  );
}
