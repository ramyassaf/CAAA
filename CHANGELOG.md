# Changelog

All notable changes to CAAA are documented here.

## May 2026 - Three-module modularization

### Added

- Three Gradle modules replacing the previous single-module setup:
  - `:domain` for pure Kotlin domain models, repository contracts, result/error abstractions, and use cases.
  - `:data` for Room, Retrofit, DTOs/entities, mappers, repository implementation, technical exception mapping, and data-level DI.
  - `:app` for Compose UI, navigation, ViewModels, app startup, app-level DI, resources, and presentation mapping.
- Gradle test fixtures for shared test helpers:
  - `:domain` owns canonical `Joke` samples and `FakeJokeRepository`.
  - `:data` owns DTO/entity factories and reuses `:domain` fixtures.
- `AppLayerArchitectureTest` to enforce the composition-root rule: only `ChiApplication` and the app DI package may import `com.compose.chi.data.*`; presentation code must stay on domain abstractions.
- App-layer Retrofit guardrail preventing `:app` production code from referencing `retrofit2` types.
- `docs/modularization.md` documenting the module layout, ownership rules, DI split, test fixture strategy, architecture-test placement, verification commands, and common pitfalls.

### Changed

- Production sources moved to their owning modules without changing package names:
  - `:domain` owns `Joke`, `JokeRepository`, `Resource`, `DomainError`, and the seven use cases.
  - `:data` owns `AppDatabase`, `JokeDao`, `JokeEntity`, `JokeApi`, `JokeDto`, `JokeRepositoryImpl`, `NetworkConfig`, and `dataKoinModule`.
  - `:app` owns `ChiApplication`, Compose screens, navigation, ViewModels, `DomainErrorUiMapper`, theme, analytics, and `appKoinModule`.
- Dependency injection split by ownership: `dataKoinModule` lives in `:data`, `appKoinModule` stays in `:app`, and `ChiApplication` loads both at startup.
- Tests now follow module ownership:
  - use-case tests in `:domain`,
  - mapper and repository implementation tests in `:data`,
  - DAO instrumented tests in `:data:androidTest`,
  - ViewModel and app-layer architecture tests in `:app`.
- Konsist architecture tests are colocated with the modules they verify, while project-wide/app-boundary rules stay in `:app`.
- Root Gradle configuration now uses plugins-DSL aliases driven by `gradle/libs.versions.toml` instead of the legacy `buildscript { classpath(...) }` pattern.
- `:domain` applies `kotlin-jvm` through the version-catalog plugin alias and uses `kotlin.jvmToolchain(17)` for JVM targeting.
- `.gitignore` now excludes module-level `build/` directories.
- App namespace and `applicationId` are aligned to `com.compose.chi`.

### Removed

- Previous single-module source layout.
- Legacy Kotlin/KSP Gradle plugin library aliases from the version catalog; plugin resolution now goes through the plugins DSL.

### Notes

- The `:app` module intentionally depends on both `:domain` and `:data` because it is the Android application and Koin composition root. Presentation code inside `:app` is still guarded from importing data implementation details by Konsist.
- `:domain` is now a pure Kotlin/JVM module and is structurally ready for a future KMP-oriented migration. No KMP migration was performed in this update.
- The application ID change means an existing debug install under `com.example.chi` will not be upgraded in place. Uninstall the old debug APK before installing the new one if needed.

### Verification

- `./gradlew.bat clean`
- `./gradlew.bat test`
- `./gradlew.bat assembleDebug`

DAO instrumented tests remain device/emulator-based and should be run with `./gradlew.bat :data:connectedDebugAndroidTest` when an emulator or device is available.

### Totals after this update

- 81 JVM unit tests.
- 5 Room DAO instrumented tests.
- 86 tests total.

## May 2026 - CI verification checkpoint

### Added

- GitHub Actions CI workflow for pull requests and pushes targeting `dev` and `main`.
- Manual workflow dispatch for on-demand verification.

### Changed

- The repository now verifies the core safety net automatically with:
  - `./gradlew test`
  - `./gradlew assembleDebug`
- GitHub Actions checkout and Java setup actions were updated to current major versions.

### Fixed

- Corrected the production-source TODO architecture rule so it scans production Kotlin sources reliably across platforms.

### Notes

- CI is intentionally limited to JVM tests, Konsist architecture tests, and debug build assembly.
- Room DAO instrumented tests remain emulator/device-based manual verification.

## May 2026 - Clean Architecture boundary hardening

### Added

- Domain-safe result abstractions under `com.compose.chi.domain.result`:
  - `Resource.Success`
  - `Resource.Error`
  - `DomainError`
- `DomainError.Persistence` for Room/DAO/local persistence failures.
- Presentation-level `DomainErrorUiMapper` for mapping domain errors to user-facing messages.
- Konsist architecture tests for:
  - domain-layer dependency boundaries
  - data-layer dependency direction
  - repository contract/implementation placement
  - use-case shape and package rules
  - remote API conventions
  - project-wide wildcard import guardrails
- Retry UI for the Ten Jokes error state, allowing the user to reload after a joke-loading failure.

### Changed

- Remote one-shot repository methods now remain `suspend` functions returning domain-safe `Resource<T>` values.
- `Resource.Loading` is no longer part of the domain result contract; loading is represented by ViewModel/UI state.
- Observable Room-backed reads now return `Flow<Resource<T>>` so local persistence failures can be represented without leaking Room exceptions.
- Local write operations now return `Resource<Unit>` so Room/DAO failures are mapped inside the data layer.
- `JokeRepositoryImpl` now owns Retrofit, HTTP, IO/network, Room, DAO, persistence, and unknown technical exception mapping.
- Use cases now delegate/orchestrate over domain-safe results and no longer catch transport or persistence exceptions.
- ViewModel and use-case tests now assert domain-safe `Resource` / `DomainError` values instead of Retrofit, OkHttp, Java IO, or Room exception types.

### Removed

- `Resource.Loading` from the domain result type.
- Technical exception handling responsibility from use cases.
- The old `common.Resource` location.
- Retrofit/OkHttp/Java IO exception expectations from domain and presentation tests.

### Notes

- Coroutine `CancellationException` is intentionally rethrown during data-layer error mapping.
- Retrofit remains the networking implementation. This update hardens the architecture boundary; it does not migrate networking to Ktor.
- No KMP, SQLDelight, Detekt, ktlint, Spotless, or broad static-analysis configuration was added in this update.

### Totals after this update

- 81 JVM unit tests.
- 5 Room DAO instrumented tests.
- 86 tests total.

## May 2026 - Test expansion

### Added

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
