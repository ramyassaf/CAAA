# Changelog

All notable changes to CAAA are documented here.

### 1- Android 15+ runtime migration

#### Added
- `enableEdgeToEdge()` in `MainActivity.onCreate`.
- `LocalDarkTheme` `CompositionLocal` (`presentation/ui/theme/DarkTheme.kt`); removes `darkTheme` / `onToggleDarkMode` params from `AppNavHost` and the four screens.

#### Changed
- `targetSdk` 34 → 36.
- `TenJokesScreen`: Material 1 `pullrefresh` → Material 3 `PullToRefreshBox`.
- `AppBottomNavigation`: `NavigationBarItemColors(...)` → `NavigationBarItemDefaults.colors(...)`; height 72 → 82 dp.
- `AppTopAppBar` action icon reflects theme (`DarkMode` ↔ `LightMode`).

#### Fixed
- Oversized `TopAppBar`: `contentWindowInsets = WindowInsets(0)` on the root `Scaffold` to stop double status-bar inset.

#### Removed
- Deprecated `window.statusBarColor` `SideEffect` from `CHITheme`.
- Redundant `tools:targetApi="33"` from `AndroidManifest.xml`.
- Obsolete `@OptIn(ExperimentalMaterialApi::class)` annotations and stale Material 3 1.2.x TODO in `AppTopAppBar`.

### 2 — Toolchain modernization

#### Changed
- Kotlin 2.3.21, AGP 9.2.x (built-in Kotlin plugin), Compose BOM 2026.05.00 (Compose 1.11.x / Material 3 1.4.x), `compileSdk` 36, JDK 17.
- Versions centralized in `gradle/libs.versions.toml`.

### 3 - Koin migration

#### Added
- Koin dependencies through the version catalog.
- `KoinModules.kt` for app dependency wiring.
- Koin startup in `ChiApplication`.

#### Changed
- Compose ViewModel retrieval now uses Koin.
- `JokeDetailsViewModel` keeps `SavedStateHandle` support through navigation arguments.

#### Removed
- Manual `AppModule` dependency wiring.
- ViewModel companion factories.
- `ViewModelFactoryHelper.kt`.
