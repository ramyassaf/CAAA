# Changelog

All notable changes to CAAA are documented here.


### May 2026 - Test expansion

#### Added
- Turbine 1.2.1 (`testImplementation`) via the version catalog.
- Shared JVM test helpers under `com.compose.chi.testing`: `MainDispatcherRule`, `TestJokes`, `FakeJokeRepository`.
- Mapper tests: `JokeDtoMapperTest`, `JokeEntityMapperTest` (both directions, all fields including `isFavourite`).
- Use-case tests covering every use case:
  - Remote `Resource` flows (`GetJokeUseCase`, `GetTenJokesUseCase`, `GetJokeByIdUseCase`): Loading → Success / `HttpException` / `IOException` branches.
  - Local / delegating use cases (`ObserveLikedJokesUseCase`, `ObserveJokeLikedStatusUseCase`, `UpsertJokeUseCase`, `DeleteAllJokesUseCase`): delegation + error propagation, Turbine `awaitError` / `assertThrows`.
- `JokeRepositoryImplTest` (10 tests, MockK API + DAO): remote mapping, local mapping, `isFavourite` preservation through `observeLikedJokes`, error non-swallowing (use cases - not the repository - own error wrapping).
- ViewModel StateFlow tests with Turbine for all four screens (`JokeHomeViewModel`, `JokeDetailsViewModel`, `TenJokesViewModel`, `MyFavouriteJokesViewModel`), driven by real use cases backed by `FakeJokeRepository`.
- Room DAO instrumented test `JokeDaoTest` (in-memory `AppDatabase`, verified on Pixel 10 API 36.1).
- `docs/tests/Testing.md` summarizing the tests.

#### Fixed
- `JokeEntity.toJoke()` was silently dropping `isFavourite`; the domain model arrived from Room with the default `false`. One-line fix: pass `isFavourite = isFavourite` into the `Joke` constructor. Guarded at the mapper, repository, and DAO test layers.

#### Changed
- `GetJokeByIdUseCaseTest` rewritten: removed debug `println`s and brittle comments, switched to shared `TestJokes`, added `coVerify` that the requested id reaches the repository.

#### Removed
- Generated `ExampleUnitTest.kt`.
- Generated `ExampleInstrumentedTest.kt`.
- Unused `androidx.compose.ui:ui-test-junit4` and Compose BOM `platform(...)` from `androidTestImplementation` (no remaining consumer after the generated test was deleted, and they published a strict `kotlinx-coroutines-bom` 1.9.0 constraint).

#### Notes
- DAO test uses `runBlocking` instead of `runTest`: `androidx.test.ext:junit` 1.3.0 / `espresso-core` 3.7.0 publish a `kotlinx-coroutines-bom:{strictly 1.9.0}` constraint on the androidTest classpath that downgrades `kotlinx-coroutines-core` to 1.9.0, and `runTest` 1.11.0 then crashes at runtime with `NoSuchMethodError: runBlockingK$default`. Strict-vs-strict cannot be overridden by `force()`, `eachDependency.useVersion()`, or another `strictly()` in modern Gradle. Per the Phase 4 guardrail, the DAO test uses `runBlocking` from `kotlinx-coroutines-core` (already on the classpath, no extra dependency, functionally equivalent for DAO-level tests).
- `MyFavouriteJokesViewModel.isLoading` is observed to be permanently `false` - initial value and `combine` both produce `isLoading = false`. Reported, not fixed (out of Phase 4 scope).

#### Totals
- 53 tests (48 JVM unit + 5 Room DAO instrumented), 0 failures.
- Production code touched: one line (`JokeEntity.toJoke()`).
- No Mockito, no Robolectric, no `room-testing`.


### May 2026 - Android 15+ runtime migration

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

### 2 - Toolchain modernization

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
