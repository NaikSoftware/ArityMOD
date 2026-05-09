## Project

ArityMOD is an Android calculator and function grapher. Single-module Gradle app written in Java.

## Build and run

```bash
./gradlew assembleDebug
./gradlew installDebug
./gradlew lint
./gradlew clean
```

`local.properties` (gitignored) must point at an Android SDK.

## Local libraries

`app/libs/` holds binary dependencies wired in via a `flatDir` repository in the root Gradle script:

- A math expression engine that owns all parsing and evaluation.
- A small wrapper used to ferry non-`Parcelable` Java objects between activities through `Intent` extras by holding a live reference in-process.

Removing the `flatDir` entry breaks AAR resolution.

## Architecture

Single Java package. Key structural ideas:

- **Process-global evaluator state.** The expression engine and the user-defined function table live as static fields, so activities share a single evaluation scope. User-defined functions sit in a stacked frame above the engine's built-ins.
- **Versioned binary persistence.** Persistent state (history, user definitions) is written through a small abstract file handler that prefixes each file with a version int. Schema changes require bumping the version; mismatches invalidate the file.
- **Shared themed activity base.** All activities extend a common base that swaps the theme based on a shared preference before `super.onCreate`. Toggling dark mode finishes the current activity so it is recreated with the new theme.
- **Custom input pipeline.** A custom `Editable` intercepts insertions to canonicalize operators to Unicode forms, collapse repeated operators, prepend the previous answer when a leading operator is typed, and prevent duplicates. Operator detection across the codebase uses the Unicode forms.
- **Custom on-screen keyboard.** A custom `View` replaces the deprecated framework keyboard. Layouts differ between portrait and landscape. The system soft keyboard is suppressed.
- **Manual orientation handling.** The main activity declares config-change handling in the manifest and rebuilds its view tree itself on rotation. Avoid relying on view state surviving only across `onCreate`.
- **Pluggable graphing backends.** Multiple renderers sit behind a single `Grapher` interface: a 2D canvas plotter (also used for multi-function plots) and a GL-based 3D renderer. The 3D path is currently incomplete. Older 3D classes remain in the tree as legacy reference. The fullscreen graph activity selects a backend by function arity.
- **Evaluation routing.** Input is split on `;`, each slice is compiled, syntax errors are swallowed silently, and results are routed by arity: scalars render as text, single- and two-variable functions feed the grapher, multiple expressions form a multi-function 2D plot.

## Conventions and gotchas

- Operators in source are Unicode literals, not ASCII. Search and pattern matching must use the Unicode forms.
- The cross-activity object-passing wrapper relies on a shared in-process reference. It is not safe across process boundaries.
- Old API-level checks remain from the original Eclipse-era code and are dead branches at the current `minSdk`. They are not load-bearing but also not actively maintained.
- Screenshot saving uses legacy external-storage APIs and will not work on modern scoped storage without changes.
- Layouts are split across default, portrait, and landscape resource folders. Edits to a shared layout may need parallel edits in the orientation-specific variants.

## Code navigation

Prefer Serena's symbolic tools (find_symbol, get_symbols_overview, find_referencing_symbols) over reading whole files. Work symbol by symbol — get an overview first, drill into bodies only when needed.
