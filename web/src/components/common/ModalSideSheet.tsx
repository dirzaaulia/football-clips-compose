'use client';

import React, { useEffect, useCallback } from 'react';
import { X } from 'lucide-react';

interface ModalSideSheetProps {
  isOpen: boolean;
  onClose: () => void;
  title?: React.ReactNode;
  headerAction?: React.ReactNode;
  widthClass?: string;
  children: React.ReactNode;
}

export default function ModalSideSheet({
  isOpen,
  onClose,
  title,
  headerAction,
  widthClass = 'sm:max-w-[580px]',
  children,
}: ModalSideSheetProps) {
  const handleKeyDown = useCallback(
    (e: KeyboardEvent) => {
      if (e.key === 'Escape') onClose();
    },
    [onClose]
  );

  useEffect(() => {
    if (isOpen) {
      document.body.style.overflow = 'hidden';
      window.addEventListener('keydown', handleKeyDown);
    } else {
      document.body.style.overflow = '';
    }
    return () => {
      document.body.style.overflow = '';
      window.removeEventListener('keydown', handleKeyDown);
    };
  }, [isOpen, handleKeyDown]);

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex justify-end">
      {/* Scrim / Backdrop */}
      <div
        onClick={onClose}
        aria-hidden="true"
        className="fixed inset-0 bg-black/65 backdrop-blur-sm transition-opacity animate-in fade-in duration-200"
      />

      {/* Side Sheet Container: Slide-up on mobile, Slide-left on desktop */}
      <div
        role="dialog"
        aria-modal="true"
        className={`relative z-10 w-full ${widthClass} bg-[#181C20] border-t sm:border-t-0 sm:border-l border-[#334155] shadow-2xl flex flex-col h-[90vh] sm:h-full mt-auto sm:mt-0 rounded-t-3xl sm:rounded-none sm:rounded-l-3xl overflow-hidden animate-in slide-in-from-bottom sm:slide-in-from-right duration-250 ease-out`}
      >
        {/* Header Bar */}
        <div className="flex items-center justify-between px-5 py-3.5 border-b border-[#334155]/60 bg-[#101418] shrink-0">
          <div className="flex items-center gap-2.5 min-w-0">
            {typeof title === 'string' ? (
              <h2 className="text-sm sm:text-base font-extrabold text-white truncate tracking-tight">
                {title}
              </h2>
            ) : (
              title
            )}
          </div>

          <div className="flex items-center gap-2 shrink-0">
            {headerAction}
            <button
              onClick={onClose}
              aria-label="Close sheet"
              className="p-1.5 rounded-xl text-[#94A3B8] hover:text-white hover:bg-[#1E2227] transition-all cursor-pointer"
            >
              <X className="w-5 h-5" />
            </button>
          </div>
        </div>

        {/* Content Body */}
        <div className="flex-1 overflow-y-auto scrollbar-none flex flex-col">
          {children}
        </div>
      </div>
    </div>
  );
}
