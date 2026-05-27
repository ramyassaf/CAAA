# CAAA — Clean Architecture Jetpack Compose Android Skeleton

> **Status:** Actively maintained and continuously modernized. The project now uses a three-module Clean Architecture setup (`:domain`, `:data`, `:app`) with Android toolchain/runtime modernization, Koin dependency injection, expanded test coverage, Konsist architecture guardrails, and GitHub Actions CI verification. The next major milestone is Phase 6: static analysis and formatting enforcement with ktlint, Spotless, and Detekt.

## What this project is

**The primary goal of this repository is to demonstrate a complete, idiomatic implementation of Clean Architecture in a modern Android application built with Jetpack Compose.**

CAAA is a small but architecturally complete Android skeleton built around a single feature: jokes from [official-joke-api.appspot.com](https://official-joke-api.appspot.com). The feature surface is intentionally narrow so the architecture remains the focus.

The codebase is intended to serve as:

- A reference for **junior and mid-level Android developers** who want to see Clean Architecture and SOLID principles applied in a modern Android app.
- A **starting point for new Android projects** that need clear architectural boundaries from day one.
- A **portfolio artifact** demonstrating the author’s approach to Android architecture, modernization, testing, and maintainability.

## Architecture overview

The project follows a three-layer Clean Architecture model enforced by Gradle modules:

```text
:app  ───────► :domain
  │              ▲
  └───────► :data
             │
             └────► :domain
```
At the Gradle level, `:app` depends on `:data` because the Android application module owns app startup and Koin composition. At the Clean Architecture layer level, presentation still follows the stricter dependency rule:

```text
presentation ─────► domain ◄───── data
   UI + VM          use cases       repository impl
   UI state         contracts       Room / Retrofit
```

- **`:domain`** — Pure Kotlin business contracts and use cases. Contains the `Joke` domain model, `JokeRepository` interface, domain-safe result/error abstractions, and use cases. It has no Android dependency, no dependency on `:data` or `:app`, and no knowledge of Retrofit, OkHttp, Room, Koin, DTOs, entities, DAOs, or UI classes.
- **`:data`** — Infrastructure and implementation details. Contains `JokeRepositoryImpl`, the Room database (`AppDatabase`, `JokeDao`, `JokeEntity`), the Retrofit API (`JokeApi`, `JokeDto`), data mappers, network configuration, and the data-level Koin module. It owns technical exception mapping and depends on `:domain`.
- **`:app`** — Android application and composition root. Contains Compose screens, `ViewModel`s, navigation, UI error mapping, theme, app startup, and app-level Koin wiring. It depends on `:domain` for presentation logic and on `:data` only to load concrete implementations at the composition root.

The important distinction is that `:app` as an Android application module can see `:data`, but presentation code inside `:app` must not use data implementation details directly. A Konsist rule enforces that only `ChiApplication` and the app DI package may import `com.compose.chi.data.*`.

Detailed modularization documentation is available in [`docs/modularization.md`](docs/modularization.md).

### Key architectural decisions

- **Dependency inversion is real, not nominal.** ViewModels depend on use cases; use cases depend on the repository interface; the repository implementation lives in the data layer behind that interface.
- **The domain module is infrastructure-free.** Domain code does not import Retrofit, OkHttp, Room, Android, AndroidX, Koin, Java IO/network APIs, DTOs, entities, DAOs, APIs, databases, or data-layer implementations.
- **DTOs, entities, and domain models are distinct.** `JokeDto` (network), `JokeEntity` (Room), and `Joke` (domain) are separate models with explicit mapping functions in the data layer.
- **Domain results are domain-safe.** `Resource<T>` represents `Success` or `Error`; loading is owned by ViewModel/UI state, not by the domain result type.
- **Technical failures stop at the data boundary.** Retrofit, HTTP, IO/network, Room, DAO, and persistence failures are caught in `JokeRepositoryImpl` and mapped to `DomainError`.
- **Coroutine cancellation is not swallowed.** Data-layer error mapping preserves `CancellationException` propagation.
- **Remote one-shot operations stay one-shot.** Remote repository methods are `suspend` functions returning `Resource<T>`.
- **Observable local reads remain reactive.** Room-backed reads use `Flow<Resource<T>>` so persistence failures can be represented without leaking Room exceptions.
- **Use cases are minimal and single-purpose.** Each use case exposes one `operator fun invoke` and delegates or orchestrates one operation without catching technical exceptions.
- **Architecture rules are tested.** Konsist tests guard domain purity, data placement, repository contracts, use-case shape, app/data separation, and project-wide hygiene.
- **Koin dependency injection is used.** Koin handles dependency wiring while preserving constructor-injection patterns and Clean Architecture boundaries.

## Tech stack

| Concern | Choice |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose, Material 3 |
| Navigation | Navigation Compose (single Activity, no Fragments) |
| Async | Kotlin Coroutines, Flow, StateFlow |
| Local storage | Room |
| Networking | Retrofit 2, OkHttp, Gson |
| DI | Koin |
| Testing | JUnit 4, MockK, `kotlinx-coroutines-test`, Turbine, Konsist, in-memory Room DAO tests, Gradle test fixtures |
| CI | GitHub Actions (`./gradlew test`, `./gradlew assembleDebug`) |
| Build | Gradle Kotlin DSL with Version Catalog (`libs.versions.toml`) |
| Annotation processing | KSP (Room compiler) |

## Project structure

```text
.
├── app/                         # Android application, UI, ViewModels, navigation, app DI
│   └── src/main/java/com/compose/chi/
│       ├── ChiApplication.kt    # Koin startup and composition root
│       ├── analytics/           # App-specific analytics abstraction
│       ├── di/                  # App-level Koin module: use cases + ViewModels
│       └── presentation/        # Compose UI, navigation, theme, UI error mapping
├── data/                        # Android library: infrastructure implementations
│   └── src/main/java/com/compose/chi/data/
│       ├── database/            # Room database, DAO, entity
│       ├── di/                  # Data-level Koin module
│       ├── remote/              # Retrofit API, DTO, network config
│       └── repository/          # JokeRepositoryImpl
└── domain/                      # Pure Kotlin/JVM module
    └── src/main/java/com/compose/chi/domain/
        ├── model/               # Joke
        ├── repository/          # JokeRepository interface
        ├── result/              # Resource<T>, DomainError
        └── use_case/            # 7 use cases
```

## Features implemented

The four screens cover the full architectural pipeline:

- **Random Joke** — fetches one joke from the network; the favourite state is reactively observed from Room.
- **Ten Jokes** — fetches ten jokes, supports pull-to-refresh, navigates to detail, and exposes a retry action after loading failures.
- **Joke Details** — fetches a joke by ID using `SavedStateHandle` navigation arguments and supports liking from the detail screen.
- **My Favourite Jokes** — observes Room-backed favourite jokes and supports clearing all favourites.

Bottom navigation, nested navigation graphs, multiple back stacks, and dark/light theme toggle are implemented.

## Testing strategy

The project has a regression safety net covering behavior and architecture across all three modules.

The current test suite covers:

- **Use cases** — all seven use cases are tested in `:domain` as delegation/orchestration units over domain-safe `Resource` values.
- **Repository implementation** — `JokeRepositoryImpl` is tested in `:data` against mocked API and DAO dependencies for remote success/error mapping, local persistence mapping, cancellation propagation, and mapper usage.
- **Domain-safe error handling** — Retrofit, HTTP, IO/network, Room, DAO, persistence, unknown, and cancellation paths are covered at the data boundary.
- **Mappers** — DTO/entity/domain mapping is tested explicitly, including `isFavourite` preservation.
- **ViewModels** — all four screen ViewModels are tested in `:app` with Turbine and a shared domain test fixture repository to verify StateFlow behavior.
- **Room DAO** — instrumented tests live in `:data` and use an in-memory Room database for insert, query, favourite filtering, liked-state lookup, and delete-all behavior.
- **Architecture rules** — Konsist tests enforce domain purity, data/repository placement, use-case shape, app/data separation, remote API conventions, project-wide wildcard import rules, and clean-boundary restrictions.
- **Shared test fixtures** — domain fixtures provide canonical `Joke` samples and `FakeJokeRepository`; data fixtures provide DTO/entity factories without duplicating domain test helpers.

Current totals:

- **81 JVM unit tests**
- **5 Room DAO instrumented tests**
- **86 tests total**

Generated template tests were removed and replaced with meaningful coverage.

GitHub Actions verifies the core safety net on pull requests and pushes targeting `dev` and `main`, with manual dispatch available when needed. The CI workflow runs `./gradlew test` and `./gradlew assembleDebug`, covering JVM tests, Konsist architecture checks, and debug build assembly.

Detailed testing documentation is available in `docs/tests/Testing.md`, including the test layout, shared helpers, architecture checks, CI verification, and guidance for adding new tests.

## Technologies checklist

| #  | Item                                                             | Status |
| -- | ---------------------------------------------------------------- | :----: |
| 1  | Kotlin                                                           |   ✅   |
| 2  | Clean Architecture with strict domain boundaries                 |   ✅   |
| 3  | MVVM                                                             |   ✅   |
| 4  | Jetpack Compose + Navigation (single Activity, no Fragments)     |   ✅   |
| 5  | REST API with OkHttp + Retrofit2                                 |   ✅   |
| 6  | Database caching with Room                                       |   ✅   |
| 7  | Use cases with dependency inversion                              |   ✅   |
| 8  | Kotlin Coroutines + Flow + StateFlow                             |   ✅   |
| 9  | Koin Dependency Injection                                        |   ✅   |
| 10 | Dependency management with Gradle Kotlin DSL + Version Catalog   |   ✅   |
| 11 | Kotlin 2.x + Compose modernization                               |   ✅   |
| 12 | Expanded unit tests across use cases, repository, mappers, VMs   |   ✅   |
| 13 | Turbine-based ViewModel StateFlow tests                          |   ✅   |
| 14 | Room DAO in-memory instrumented tests                            |   ✅   |
| 15 | Domain-safe result/error model                                   |   ✅   |
| 16 | Data-owned technical exception mapping                           |   ✅   |
| 17 | Konsist architecture boundary tests                              |   ✅   |
| 18 | GitHub Actions CI verification                                   |   ✅   |
| 19 | Three-module modularization (`:domain`, `:data`, `:app`)         |   ✅   |
| 20 | Gradle test fixtures for shared test helpers                     |   ✅   |
| 21 | Static analysis with ktlint / Spotless / Detekt                  |   ⏳   |
| 22 | Offline-first repository pattern                                 |   ⏳   |
| 23 | MockWebServer API integration tests                              |   ⏳   |
| 24 | Network connectivity monitoring                                  |   ⏳   |
| 25 | DataStore                                                        |   ⏳   |
| 26 | Kotlin Multiplatform exploration                                 |   ⏳   |

## Roadmap

The repository is modernized incrementally so each step leaves the app buildable, reviewable, and easier to evolve. The current order is intentional: architecture cleanup, testing, CI, and modularization come before broader static analysis and behavior-changing data-flow work.

Completed:

- **Architectural cleanup**
  - Immutability cleanup.
  - Repository Flow simplifications.
  - ViewModel collector re-entry fix.
  - Mockito removal and MockK cleanup.

- **Toolchain modernization**
  - Kotlin 2.3.21.
  - AGP 9.2.1 / Gradle 9.5.1.
  - Compose BOM 2026.05.00.
  - SDK 36.
  - KSP and Compose compiler plugin modernization.

- **Android 15+ runtime migration**
  - Edge-to-edge support.
  - Material 3 pull-to-refresh migration.
  - Top app bar/system inset cleanup.

- **Experimental API audit**
  - Existing Material 3 experimental opt-ins reviewed and intentionally retained where still required.

- **Koin migration**
  - Manual dependency graph removed.
  - Koin module wiring added.
  - ViewModels moved to constructor-injected Koin creation.
  - `SavedStateHandle` support preserved for joke details.

- **Expanded test safety net**
  - Use-case tests.
  - Mapper tests.
  - Repository implementation tests.
  - ViewModel StateFlow tests with Turbine.
  - Room DAO in-memory instrumented tests.
  - Generated example tests removed.

- **Clean Architecture boundary hardening**
  - Domain result types moved under `domain.result`.
  - `Resource.Loading` removed from the domain result type.
  - Loading moved to ViewModel/UI state.
  - Retrofit, HTTP, IO/network, Room, DAO, and persistence failures mapped inside the data repository implementation.
  - Use cases simplified to delegation/orchestration over domain-safe results.

- **Konsist architecture guardrails**
  - Domain, data, repository, use-case, API, app-layer, and project-wide architecture tests added.
  - Wildcard import checks added.
  - Clean Architecture boundary regressions are now covered by unit tests.

- **CI verification checkpoint**
  - GitHub Actions verifies pull requests and pushes targeting `dev` and `main`.
  - Manual workflow dispatch is available for on-demand verification.
  - CI runs `./gradlew test` and `./gradlew assembleDebug`.
  - The production-source TODO architecture rule was corrected to scan production sources reliably across platforms.

- **Three-module modularization**
  - The previous single Android module was split into `:domain`, `:data`, and `:app`.
  - Package-level Clean Architecture boundaries are now backed by Gradle module boundaries and Konsist app-layer rules.
  - Tests and architecture suites were moved to the modules that own the code they verify.
  - Shared domain/data test helpers were consolidated through Gradle test fixtures.

Planned:

- **Static analysis and formatting enforcement**
  - Add ktlint, Spotless, and Detekt.
  - Keep the existing Konsist architecture rules and expand only where useful.

- **Offline-first repository pattern**
  - Move repository behavior toward cached-first flows backed by Room.
  - Emit cached data first, then refresh from the network and persist updates.

- **Kotlin Multiplatform**
  - Evaluate moving domain and data foundations toward shared Kotlin code.
  - Prepare for Room KMP, Ktor networking, and shared dependency wiring.

## Considered

The following are intentionally not included to keep the architectural focus clear:

- Firebase services such as FCM, Analytics, or Crashlytics.
- Multiple unrelated features; the project remains single-feature by design.

## About

Created by Ramy ASSAF, Senior Mobile Engineer with a focus on Android architecture, modernization, and maintainability of mobile projects.

This repository is a deliberate teaching artifact: a compact, single-feature codebase used to demonstrate how Clean Architecture and SOLID principles translate into idiomatic modern Android. The scope is intentionally narrow so the architectural decisions remain legible.

## Resources

- [The Clean Architecture — Robert C. Martin (Uncle Bob)](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [Guide to app architecture — Android Developers](https://developer.android.com/topic/architecture)
- [Domain layer — Android Developers](https://developer.android.com/topic/architecture/domain-layer)
- [Testing Kotlin flows on Android — Android Developers](https://developer.android.com/kotlin/flow/test)
