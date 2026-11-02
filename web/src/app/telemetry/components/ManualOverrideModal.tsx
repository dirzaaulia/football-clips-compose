import React, { useState } from 'react';
import { X, Video, Check, AlertCircle, Loader2 } from 'lucide-react';
import { database } from '@/lib/database';

interface ManualOverrideModalProps {
  matchId: string;
  matchTitle: string;
  isOpen: boolean;
  onClose: () => void;
  onSuccess: () => void;
}

function extractYouTubeId(input: string): string | null {
  const trimmed = input.trim();
  if (/^[a-zA-Z0-9_-]{11}$/.test(trimmed)) {
    return trimmed;
  }
  const match = trimmed.match(
    /(?:youtu\.be\/|youtube\.com\/(?:embed\/|v\/|watch\?v=|watch\?.+&v=))([\w-]{11})/
  );
  return match ? match[1] : null;
}

export function ManualOverrideModal({
  matchId,
  matchTitle,
  isOpen,
  onClose,
  onSuccess,
}: ManualOverrideModalProps) {
  const [videoInput, setVideoInput] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);
  const [successMsg, setSuccessMsg] = useState<string | null>(null);

  if (!isOpen) return null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMsg(null);
    setSuccessMsg(null);

    const videoId = extractYouTubeId(videoInput);
    if (!videoId) {
      setErrorMsg('Invalid YouTube URL or Video ID. Expected 11-character alphanumeric ID.');
      return;
    }

    setIsSubmitting(true);
    try {
      // 1. Update matches row in datastore
      const { error: dbError } = await database
        .from('matches')
        .update({
          highlight_video_id: videoId,
          status: 'FINISHED',
          updated_at: new Date().toISOString(),
        })
        .eq('id', matchId);

      if (dbError) {
        throw new Error(dbError.message || 'Database update failed');
      }

      // 2. Non-blocking Edge cache purge
      fetch('https://edge.fc.dirzaaulia.com/api/purge-cache', { method: 'POST' }).catch(() => {});

      setSuccessMsg(`Successfully linked YouTube Video ID: ${videoId}`);
      setTimeout(() => {
        onSuccess();
        onClose();
      }, 1200);
    } catch (err: any) {
      setErrorMsg(err?.message || 'Failed to update highlight video.');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm animate-fade-in">
      <div className="relative w-full max-w-md bg-slate-900 border border-slate-800 rounded-2xl shadow-2xl p-6 space-y-4">
        {/* Header */}
        <div className="flex items-center justify-between border-b border-slate-800/80 pb-3">
          <div className="flex items-center gap-2">
            <div className="w-8 h-8 rounded-lg bg-red-500/10 flex items-center justify-center text-red-500">
              <Video className="w-5 h-5" />
            </div>
            <div>
              <h3 className="text-sm font-bold text-slate-100">Force Link Highlight</h3>
              <p className="text-xs font-mono text-slate-400">Match ID: {matchId}</p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-1 rounded-lg text-slate-400 hover:text-slate-200 hover:bg-slate-800 transition"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        {/* Content */}
        <div className="space-y-1">
          <p className="text-xs text-slate-300 font-semibold">{matchTitle}</p>
          <p className="text-xs text-slate-400">
            Paste an official YouTube video URL or 11-char Video ID to manually associate this highlight.
          </p>
        </div>

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-xs font-mono font-medium text-slate-300 mb-1">
              YouTube Video ID or URL
            </label>
            <input
              type="text"
              value={videoInput}
              onChange={(e) => setVideoInput(e.target.value)}
              placeholder="e.g. dQw4w9WgXcQ or https://youtu.be/..."
              disabled={isSubmitting}
              className="w-full bg-slate-950 border border-slate-800 rounded-lg px-3 py-2 text-xs font-mono text-slate-100 placeholder-slate-500 focus:outline-none focus:border-indigo-500 transition"
            />
          </div>

          {errorMsg && (
            <div className="flex items-center gap-2 p-2.5 rounded-lg bg-rose-500/10 border border-rose-500/30 text-rose-400 text-xs font-mono">
              <AlertCircle className="w-4 h-4 shrink-0" />
              <span>{errorMsg}</span>
            </div>
          )}

          {successMsg && (
            <div className="flex items-center gap-2 p-2.5 rounded-lg bg-emerald-500/10 border border-emerald-500/30 text-emerald-400 text-xs font-mono">
              <Check className="w-4 h-4 shrink-0" />
              <span>{successMsg}</span>
            </div>
          )}

          <div className="flex items-center justify-end gap-2 pt-2">
            <button
              type="button"
              onClick={onClose}
              disabled={isSubmitting}
              className="px-3.5 py-1.5 rounded-lg text-xs font-mono text-slate-400 hover:text-slate-200 hover:bg-slate-800 transition"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={isSubmitting || !videoInput.trim()}
              className="inline-flex items-center gap-1.5 px-4 py-1.5 rounded-lg text-xs font-mono font-semibold bg-indigo-600 hover:bg-indigo-500 text-white transition disabled:opacity-50 disabled:cursor-not-allowed shadow-sm"
            >
              {isSubmitting ? (
                <>
                  <Loader2 className="w-3.5 h-3.5 animate-spin" />
                  <span>Linking...</span>
                </>
              ) : (
                <span>Force Link Video</span>
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
