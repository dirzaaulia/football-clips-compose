# FootballClips Hermetic E2E & Responsive Matrix Verification Report

- **Timestamp**: `2026-11-08 02:16:25 UTC`
- **Overall Status**: `PASSED (100%)`
- **Selected Journey**: `all`
- **Selected Device Matrix**: `all`

## Suite Execution Waterfall

| Layer / Suite | Status | Duration (s) | Summary |
| :--- | :--- | :---: | :--- |
| **Python Backend & E2E Lifecycle Suite (pytest)** | `PASS` | `106.27s` | 71 passed, 2 warnings in 105.20s (0:01:45) |
| **Next.js 16 Web Vitest Suite (Geoblocking, Webhook, Excitement, Responsive)** | `PASS` | `1.82s` | Set 'VITE_CONFIG_NATIVE_IGNORE_WARNING=true' to suppress this warning.[39m |
| **Android & CMP Host Suite + 6-Form-Factor Device Matrix** | `PASS` | `7.79s` | Configuration cache entry reused. |

## Covered Device Matrix Form Factors

| Form Factor | Dimensions (`dp`) | Window Class | Navigation Chrome | Grid Columns (Highlights / Fixtures) |
| :--- | :---: | :---: | :--- | :---: |
| **Compact Phone** | `390 x 844` | `Compact` | Floating Bottom Pill Nav | `1 / 1` |
| **Wide Portrait Phone** | `640 x 960` | `Medium` | Floating Bottom Pill Nav + Side Sheet | `2 / 2` |
| **Foldable Cover** | `380 x 900` | `Compact` | Floating Bottom Pill Nav | `1 / 1` |
| **Foldable Unfolded** | `720 x 820` | `Medium` | Floating Bottom Pill Nav + Side Sheet | `3 / 2` |
| **Landscape Flex Mode** | `840 x 390` | `Medium` | Left Navigation Rail + Dual-Pane Split Deck | `2 / 2` |
| **Tablet / Desktop** | `1024 x 768` | `Expanded` | Left Navigation Rail + Persistent Side Sheet | `3 / 2` |
