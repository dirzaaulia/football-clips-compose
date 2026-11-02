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
        position: { x: 20, y: 50 },
        data: {
          title: 'Stage 1',
          subtitle: 'ESPN Sports Engine',
          badge: '0 QUOTA',
          badgeColor: 'bg-emerald-500/20 text-emerald-400',
          metric: '1m sync cadence',
          filterKey: 'ESPN',
        },
      },
      {
        id: '2',
        type: 'pipelineNode',
        position: { x: 270, y: 50 },
        data: {
          title: 'Stage 2',
          subtitle: 'Cloud Tasks Queue',
          badge: 'ISOLATED',
          badgeColor: 'bg-blue-500/20 text-blue-400',
          metric: '2 tasks/sec limit',
          filterKey: 'TASK',
        },
      },
      {
        id: '3',
        type: 'pipelineNode',
        position: { x: 520, y: 10 },
        data: {
          title: 'Stage 3A',
          subtitle: 'YouTube Discovery',
          badge: '60K QUOTA',
          badgeColor: 'bg-purple-500/20 text-purple-400',
          metric: `${todayQuotaUsage.toLocaleString()} units`,
          filterKey: 'YOUTUBE',
        },
      },
      {
        id: '4',
        type: 'pipelineNode',
        position: { x: 520, y: 130 },
        data: {
          title: 'Stage 3B',
          subtitle: 'WebSub Push Webhook',
          badge: '<100ms',
          badgeColor: 'bg-amber-500/20 text-amber-400',
          metric: '0 API quota',
          filterKey: 'WEBSUB',
        },
      },
      {
        id: '5',
        type: 'pipelineNode',
        position: { x: 770, y: 70 },
        data: {
          title: 'Stage 4',
          subtitle: 'Oracle AI Gateway',
          badge: 'DEEPSEEK',
          badgeColor: 'bg-emerald-500/20 text-emerald-400',
          metric: 'Gemini Lite fallback',
          filterKey: 'AI',
        },
      },
      {
        id: '6',
        type: 'pipelineNode',
        position: { x: 1020, y: 70 },
        data: {
          title: 'Stage 5',
          subtitle: 'PostgreSQL & Edge',
          badge: 'SUB-15ms',
          badgeColor: 'bg-cyan-500/20 text-cyan-400',
          metric: 'Anycast cache purge',
          filterKey: 'POSTGRESQL',
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
        id: 'e3-5',
        source: '3',
        target: '5',
        animated: true,
        style: { stroke: '#10B981', strokeWidth: 2 },
        markerEnd: { type: MarkerType.ArrowClosed, color: '#10B981' },
      },
      {
        id: 'e4-5',
        source: '4',
        target: '5',
        animated: true,
        style: { stroke: '#10B981', strokeWidth: 2 },
        markerEnd: { type: MarkerType.ArrowClosed, color: '#10B981' },
      },
      {
        id: 'e5-6',
        source: '5',
        target: '6',
        animated: true,
        style: { stroke: '#06B6D4', strokeWidth: 2 },
        markerEnd: { type: MarkerType.ArrowClosed, color: '#06B6D4' },
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
