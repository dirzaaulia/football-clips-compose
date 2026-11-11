/**
 * Contrast Guard Utility for Dark Mode UI
 * Ensures club kit & primary colors remain vibrant and legible against dark (#101418 / #0F172A) backgrounds.
 */

export function ensureLegibleHex(
  hex: string | null | undefined,
  fallbackHex: string = '#7098F8',
): string {
  if (!hex) return fallbackHex;
  let clean = hex.trim().replace(/^#/, '');
  if (clean.length === 3) {
    clean = clean.split('').map((c) => c + c).join('');
  }
  if (clean.length !== 6) return fallbackHex;

  const r = parseInt(clean.substring(0, 2), 16);
  const g = parseInt(clean.substring(2, 4), 16);
  const b = parseInt(clean.substring(4, 6), 16);
  if (isNaN(r) || isNaN(g) || isNaN(b)) return fallbackHex;

  // Calculate perceived relative luminance
  const lum = (0.299 * r + 0.587 * g + 0.114 * b) / 255;
  if (lum < 0.22) {
    // Too dark to be seen on dark stadium pitch / dark card; boost vibrancy
    const factor = 2.4;
    const nr = Math.min(255, Math.max(65, Math.round(r * factor + 55)));
    const ng = Math.min(255, Math.max(65, Math.round(g * factor + 55)));
    const nb = Math.min(255, Math.max(65, Math.round(b * factor + 55)));
    return `#${nr.toString(16).padStart(2, '0')}${ng.toString(16).padStart(2, '0')}${nb.toString(16).padStart(2, '0')}`;
  }
  return `#${clean}`;
}
