# Report UI design verification

final result: passed

## Scope and evidence

Implement the three approved screens in the existing Thymeleaf/Bootstrap report server. No new dependencies or changes to report storage/contracts.

- Source directory: `/Users/dongjin/.codex/generated_images/01a0d792-44c1-73f0-921a-9e0e42fb538f/`
- Service source: `exec-dbf77a47-08ad-46e5-8b22-13f0fe9f1591.png`
- History source: `exec-55d24d4d-89b0-4891-8ec8-610da6ebc095.png`
- Detail source: `exec-58c8df12-3662-4b98-a867-8ccf2cdebe3e.png`
- Browser captures: `output/playwright/ui-redesign/01-services.png`, `02-history.png`, `03-detail.png`.
- Mobile captures: `04-services-mobile.png`, `05-history-mobile.png`, `06-detail-mobile.png` in the same directory.
- Desktop CSS viewport: 1440 × 1024, devicePixelRatio 1. Generated references are 1487 × 1058; compare at the same proportional width, approximately 1440 × 1024. Accepted desktop screenshots are 1440 × 1024.
- Mobile CSS viewport: 390 × 844. Detail additionally captured as a full page.
- State: separate temporary fixture storage, four catalog services, six reservation runs, latest run with 48 cases / 46 passes / 2 failures. Detail comparison selects FAIL and the first case. These are test fixtures, not real test execution results.
- Source and rendered screenshots were emitted together for each full-view comparison. Text, controls, and failure content were legible in the full view; DOM checks and browser interactions supplemented inspection, so additional cropped comparisons were unnecessary.

## Comparison history

1. Initial implementation: P2 — filter disclosure/reset controls consumed extra vertical space in history and detail. Evidence: `02-history-initial.png`, `03-detail-initial.png`. Move disclosure alongside primary filters, move reset beside results/tabs, reduce summary spacing. Recaptured in `02-history.png`, `03-detail.png`; resolved.
2. Mobile history: P2 — API names wrapped into short fragments. Give the history table a minimum width inside an independent horizontal scrolling container. Result: readable cells without widening the page.
3. Mobile services: P2 — absolutely positioned accessible table heading widened the document to 551px at a 390px viewport. Make the table wrapper positioned. Recheck: document width equals viewport width (390px); resolved.
4. Some background-tab detail captures became blurred/clipped after opening the raw report. Reject these captures; open a fresh tab, restore a 1440 × 1024 viewport, capture with the browser hidden, and inspect the replacement `03-detail.png`.

## Fidelity assessment

- Typography: retain the native system font stack, 30px page headings, 14–15px table/control text, 18px service links, monospace API identifiers and failure details. Minor font/glyph differences from generated artwork are expected.
- Layout: dark header, wide light surface, restrained summary strip, compact primary filters, status tabs, separated table rows, and a two-column case inspector match the approved direction. At narrow widths the inspector stacks below a bounded case list.
- Colors: navy header/text, pale gray table headers, blue selection/actions, restrained red/green status badges. Text labels accompany colors.
- Assets: no raster illustrations required. Native disclosure affordances and explicit action labels replace decorative search/copy/external-link icons; no new icon dependency.
- Content: real catalog/run data replaces the mock data. Do not show the mock's “디자인 시안 · 예시 데이터” label in the application. Preserve backend service ordering and default all-time history. Preserve separate API and case-name filters rather than changing their query contracts.

## Validation

- `mise exec -- ./gradlew -p report-server test processResources`: 39 tests, 0 failures/errors/skips, including six new rendered-view tests.
- Rendered tests cover catalog/unrun state, history deep links, escaped failure messages, CATS HTTP-call cases, missing runs, and context-path-aware navigation.
- In-app browser Playwright: service search/no results/clear, organization filter, service-to-history-to-detail navigation, source and status filters/counts, case-name metadata loading, filter URL reload, date preset and reset, keyboard case selection, successful message copy, disabled copy for a passing case, detail no-results state, and raw-report navigation.
- Mobile: service/history/detail routes at 390px, no page-level horizontal overflow after fixes. History/service tables intentionally scroll horizontally; case inspector stacks vertically.
- Browser console: no captured error-level entries during checked flows.
- CodeRabbit: one minor finding on context-path navigation; corrected with Thymeleaf root links and regression test. Review covered tracked UI changes; new shared JavaScript also received direct inspection, syntax checks, and browser exercise.
- `node --check` on all three UI scripts and `git diff --check` pass.

## Limits and follow-up polish

- P3: system-font rendering and omitted decorative icons differ slightly from the generated mockups.
- No claim of complete WCAG compliance or cross-browser coverage. Clipboard-denial fallback has an explicit message but was not forced in browser testing.
- Existing original report files and their third-party layouts remain unchanged. Raw-report navigation used a local fixture to verify the route, not a fresh Karate/CATS test execution.
