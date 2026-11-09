import { describe, expect, it } from 'vitest';

export interface WebResponsiveConfig {
  viewportCategory: 'mobile' | 'tablet' | 'desktop';
  showDesktopSidebar: boolean;
  showNavigationRail: boolean;
  highlightsGridCols: number;
  playerSheetMode: 'bottom-sheet' | 'side-sheet';
}

export function resolveWebResponsiveLayout(widthPx: number): WebResponsiveConfig {
  if (widthPx < 768) {
    return {
      viewportCategory: 'mobile',
      showDesktopSidebar: false,
      showNavigationRail: true,
      highlightsGridCols: 1,
      playerSheetMode: 'bottom-sheet',
    };
  }
  if (widthPx < 1024) {
    return {
      viewportCategory: 'tablet',
      showDesktopSidebar: false,
      showNavigationRail: true,
      highlightsGridCols: 2,
      playerSheetMode: 'side-sheet',
    };
  }
  return {
    viewportCategory: 'desktop',
    showDesktopSidebar: true,
    showNavigationRail: true,
    highlightsGridCols: widthPx >= 1440 ? 4 : 3,
    playerSheetMode: 'side-sheet',
  };
}

describe('Next.js Web Responsive Viewport Matrix', () => {
  it('Adapts to Mobile viewport (< 768px) with navigation rail and 1-column grid', () => {
    const layout = resolveWebResponsiveLayout(390);
    expect(layout.viewportCategory).toBe('mobile');
    expect(layout.showNavigationRail).toBe(true);
    expect(layout.showDesktopSidebar).toBe(false);
    expect(layout.highlightsGridCols).toBe(1);
    expect(layout.playerSheetMode).toBe('bottom-sheet');
  });

  it('Adapts to Tablet / Unfolded Foldable viewport (768px - 1023px) with 2-column grid and side sheet', () => {
    const layout = resolveWebResponsiveLayout(820);
    expect(layout.viewportCategory).toBe('tablet');
    expect(layout.highlightsGridCols).toBe(2);
    expect(layout.playerSheetMode).toBe('side-sheet');
  });

  it('Adapts to Desktop viewport (>= 1024px) with persistent left sidebar and 3-4 column grid', () => {
    const desktop1024 = resolveWebResponsiveLayout(1024);
    expect(desktop1024.viewportCategory).toBe('desktop');
    expect(desktop1024.showDesktopSidebar).toBe(true);
    expect(desktop1024.highlightsGridCols).toBe(3);

    const desktop1440 = resolveWebResponsiveLayout(1440);
    expect(desktop1440.highlightsGridCols).toBe(4);
  });
});
