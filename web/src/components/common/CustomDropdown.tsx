'use client';

import React, { useState, useRef, useEffect } from 'react';
import { ChevronDown, Check } from 'lucide-react';

export interface DropdownItem<T = string | number> {
  value: T;
  label: string;
  shortLabel?: string;
  icon?: React.ReactNode;
  badge?: string;
  badgeVariant?: 'primary' | 'amber' | 'emerald' | 'gray';
  description?: string;
}

interface CustomDropdownProps<T = string | number> {
  items: DropdownItem<T>[];
  value: T;
  onChange: (value: T) => void;
  leadingIcon?: React.ReactNode;
  prefix?: string;
  variant?: 'default' | 'amber' | 'compact' | 'icon';
  align?: 'left' | 'right' | 'popout';
  className?: string;
  menuWidth?: string;
  ariaLabel?: string;
}

export default function CustomDropdown<T extends string | number>({
  items,
  value,
  onChange,
  leadingIcon,
  prefix,
  variant = 'default',
  align = 'left',
  className = '',
  menuWidth = 'min-w-full w-max max-w-[calc(100vw-1.5rem)]',
  ariaLabel = 'Select option',
}: CustomDropdownProps<T>) {
  const [isOpen, setIsOpen] = useState(false);
  const [coords, setCoords] = useState<{ top: number; left: number }>({ top: 0, left: 0 });
  const [autoAlign, setAutoAlign] = useState<'left' | 'right'>(align === 'popout' ? 'left' : align);
  const containerRef = useRef<HTMLDivElement>(null);

  const selectedItem = items.find((item) => item.value === value) || items[0];

  const handleToggle = () => {
    if (!isOpen && containerRef.current) {
      const rect = containerRef.current.getBoundingClientRect();
      if (align === 'popout') {
        setCoords({
          top: Math.max(12, Math.min(rect.top, window.innerHeight - 200)),
          left: Math.min(rect.right + 10, window.innerWidth - 220),
        });
      } else {
        // Auto-detect if trigger button is near the right edge of viewport
        const spaceOnRight = window.innerWidth - rect.left;
        if (spaceOnRight < 280 || align === 'right') {
          setAutoAlign('right');
        } else {
          setAutoAlign('left');
        }
      }
    }
    setIsOpen((prev) => !prev);
  };

  useEffect(() => {
    function handleClickOutside(event: MouseEvent | TouchEvent) {
      if (containerRef.current && !containerRef.current.contains(event.target as Node)) {
        setIsOpen(false);
      }
    }

    function handleKeyDown(event: KeyboardEvent) {
      if (event.key === 'Escape') {
        setIsOpen(false);
      }
    }

    if (isOpen) {
      document.addEventListener('mousedown', handleClickOutside);
      document.addEventListener('touchstart', handleClickOutside);
      document.addEventListener('keydown', handleKeyDown);
    }

    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
      document.removeEventListener('touchstart', handleClickOutside);
      document.removeEventListener('keydown', handleKeyDown);
    };
  }, [isOpen]);

  const isAmber = variant === 'amber' || selectedItem?.badgeVariant === 'amber';
  const isIcon = variant === 'icon';

  const triggerTitle = selectedItem?.label
    ? `${prefix ? `${prefix}: ` : ''}${selectedItem.label}`
    : ariaLabel;

  return (
    <div ref={containerRef} className={`relative inline-block text-left ${className}`}>
      {/* Trigger Button */}
      <button
        type="button"
        aria-label={ariaLabel}
        aria-expanded={isOpen}
        aria-haspopup="listbox"
        title={isIcon ? triggerTitle : undefined}
        onClick={handleToggle}
        className={`group flex items-center justify-between transition-all duration-200 cursor-pointer shadow-md select-none ${
          isIcon
            ? 'w-10 h-10 rounded-xl justify-center bg-[#181C20] border'
            : variant === 'compact'
            ? 'px-2.5 py-1.5 rounded-xl text-xs gap-2 bg-[#181C20] border'
            : 'px-3.5 py-2 rounded-2xl text-xs sm:text-sm gap-2 bg-[#181C20] border'
        } ${
          isAmber
            ? 'border-amber-500/50 bg-amber-500/10 shadow-[0_0_12px_rgba(245,158,11,0.15)] hover:border-amber-400'
            : 'border-[#334155] hover:border-[#7098F8]/60 hover:bg-[#1E232B]'
        }`}
      >
        <div className="flex items-center gap-2 whitespace-nowrap min-w-0">
          {leadingIcon && (
            <span
              className={`shrink-0 transition-transform duration-200 group-hover:scale-105 ${
                isAmber ? 'text-amber-400' : 'text-[#7098F8]'
              }`}
            >
              {leadingIcon}
            </span>
          )}
          {!isIcon && prefix && (
            <span className="text-[#94A3B8] font-medium hidden sm:inline text-xs shrink-0">
              {prefix}:
            </span>
          )}
          {!isIcon && (
            <span
              className={`font-bold truncate max-w-[130px] sm:max-w-[170px] ${
                isAmber ? 'text-amber-300' : 'text-white'
              }`}
            >
              {selectedItem?.shortLabel || selectedItem?.label || 'Select'}
            </span>
          )}
        </div>

        {!isIcon && (
          <ChevronDown
            className={`w-3.5 h-3.5 shrink-0 text-[#94A3B8] transition-transform duration-200 ${
              isOpen ? 'rotate-180 text-white' : 'group-hover:text-white'
            }`}
          />
        )}
      </button>

      {/* Popout transparent backdrop for outside clicks */}
      {isOpen && align === 'popout' && (
        <div
          className="fixed inset-0 z-[9998] bg-black/20"
          onClick={() => setIsOpen(false)}
        />
      )}

      {/* Modern Glassmorphic Dropdown Menu */}
      {isOpen && (
        <div
          role="listbox"
          aria-label={ariaLabel}
          style={
            align === 'popout'
              ? { position: 'fixed', top: `${coords.top}px`, left: `${coords.left}px`, zIndex: 9999 }
              : undefined
          }
          className={`${
            align === 'popout'
              ? ''
              : autoAlign === 'right'
              ? 'absolute right-0 mt-2 z-50'
              : 'absolute left-0 mt-2 z-50'
          } ${menuWidth} bg-[#181C20]/95 backdrop-blur-xl border border-[#334155] rounded-2xl shadow-[0_12px_36px_rgba(0,0,0,0.55)] p-1.5 focus:outline-none transition-all duration-150 origin-top animate-in fade-in-0 zoom-in-95`}
        >
          <div className="max-h-64 overflow-y-auto space-y-1 overscroll-contain">
            {items.map((item) => {
              const isSelected = item.value === value;
              const isItemAmber = item.badgeVariant === 'amber';

              return (
                <button
                  key={String(item.value)}
                  type="button"
                  role="option"
                  aria-selected={isSelected}
                  onClick={() => {
                    onChange(item.value);
                    setIsOpen(false);
                  }}
                  className={`w-full flex items-center justify-between gap-3 px-3 py-2 rounded-xl text-left text-xs sm:text-sm transition-all duration-150 cursor-pointer whitespace-nowrap ${
                    isSelected
                      ? isItemAmber
                        ? 'bg-amber-500/20 text-amber-300 border border-amber-500/30 font-bold'
                        : 'bg-[#1E3A8A]/40 text-[#7098F8] border border-[#7098F8]/30 font-bold'
                      : 'text-[#94A3B8] hover:bg-white/5 hover:text-white font-medium border border-transparent'
                  }`}
                >
                  <div className="flex items-center gap-2 whitespace-nowrap">
                    {item.icon && (
                      <span
                        className={`shrink-0 ${
                          isSelected
                            ? isItemAmber
                              ? 'text-amber-400'
                              : 'text-[#7098F8]'
                            : 'text-[#94A3B8]'
                        }`}
                      >
                        {item.icon}
                      </span>
                    )}
                    <span className="whitespace-nowrap">{item.label}</span>
                  </div>

                  <div className="flex items-center gap-2 shrink-0 ml-2">
                    {item.badge && (
                      <span
                        className={`px-2 py-0.5 rounded-md text-[10px] font-bold uppercase tracking-wider whitespace-nowrap ${
                          item.badgeVariant === 'amber'
                            ? 'bg-amber-500/20 text-amber-300 border border-amber-500/30'
                            : item.badgeVariant === 'emerald'
                            ? 'bg-emerald-500/20 text-emerald-300 border border-emerald-500/30'
                            : item.badgeVariant === 'gray'
                            ? 'bg-[#334155]/50 text-[#94A3B8]'
                            : 'bg-[#1E3A8A]/50 text-[#7098F8] border border-[#7098F8]/30'
                        }`}
                      >
                        {item.badge}
                      </span>
                    )}
                    {isSelected && (
                      <Check
                        className={`w-3.5 h-3.5 shrink-0 ${
                          isItemAmber ? 'text-amber-400' : 'text-[#7098F8]'
                        }`}
                      />
                    )}
                  </div>
                </button>
              );
            })}
          </div>
        </div>
      )}
    </div>
  );
}
