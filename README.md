# CAAA — Clean Architecture Jetpack Compose Android Skeleton

> **Status:** Actively maintained and continuously modernized. 2026 Updates done: modern Android toolchain migration, migrantion from manual to Koin dependency injection, and expanded test safety net. The next major milestone is three-module modularization (`:domain`, `:data`, `:app`) as preparation for future Kotlin Multiplatform work.

## What this project is

**The primary goal of this repository is to demonstrate a complete, idiomatic implementation of Clean Architecture in a modern Android application built with Jetpack Compose.**

CAAA is a small but architecturally complete Android skeleton, built around a single feature (jokes from [official-joke-api.appspot.com](https://official-joke-api.appspot.com)) deliberately kept small so the architecture itself remains the focus rather than the feature surface. The codebase is intended to serve as:

- A reference for **junior and mid-level Android developers** wanting to see Clean Architecture and SOLID principles applied in practice with modern Android tooling.
- A **starting point for new Android projects** that need a solid architectural foundation from day one.
- A **portfolio artifact** demonstrating the author's approach to mobile architecture.

## Architecture overview

The project follows the three-layer Clean Architecture model, expressed as packages:

- **`domain`** — Pure Kotlin business logic. Contains the `Joke` model, the `JokeRepository` interface, and all use cases. Has zero Android dependencies, no imports from `data.*`, and no knowledge of Retrofit, Room, or Compose. This is the contract that the rest of the app depends on.
- **`data`** — Implementation details. Contains `JokeRepositoryImpl`, the Room database (`AppDatabase`, `JokeDao`, `JokeEntity`), the Retrofit API (`JokeApi`, `JokeDto`), and the mapping extensions that convert between layer-specific models. Depends on `domain`.
- **`presentation`** — UI layer. Compose screens, `ViewModel`s, navigation, and theme. Depends on `domain` and consumes use cases. No direct knowledge of the data layer.

Cross-cutting concerns (`Resource<T>` sealed class, constants, analytics logger, dependency injection wiring) live in dedicated top-level packages.

### Key architectural decisions

- **Dependency inversion is real, not nominal.** ViewModels depend on use cases; use cases depend on the repository interface; the repository implementation lives in the data layer behind that interface. The domain layer literally cannot import from data.
- **DTOs, entities, and domain models are distinct.** `JokeDto` (network), `JokeEntity` (Room), and `Joke` (domain) are separate classes with explicit mapping functions kept in the data layer. The domain never sees a `@SerializedName` or an `@Entity`.
- **Use cases are minimal and single-purpose.** Each use case exposes exactly one `operator fun invoke` and orchestrates a single piece of business logic. The codebase currently includes seven: `GetJokeUseCase`, `GetTenJokesUseCase`, `GetJokeByIdUseCase`, `ObserveLikedJokesUseCase`, `ObserveJokeLikedStatusUseCase`, `UpsertJokeUseCase`, `DeleteAllJokesUseCase`.
- **Koin dependency injection.** The repository uses Koin for dependency wiring while preserving the same Clean Architecture boundaries and constructor-injection patterns established during the earlier manual-DI implementation.

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
| Testing | JUnit 4, MockK, `kotlinx-coroutines-test`, Turbine, in-memory Room DAO tests |
| Build | Gradle Kotlin DSL with Version Catalogue (`libs.versions.toml`) |
| Annotation processing | KSP (Room compiler) |

## Project structure

```
app/src/main/java/com/compose/chi/
├── analytics/        # AnalyticsLogger interface + impl (class delegation example)
├── common/           # Resource<T>, Constants
├── data/
│   ├── database/     # AppDatabase, JokeDao, JokeEntity
│   ├── remote/       # JokeApi (Retrofit), JokeDto
│   └── repository/   # JokeRepositoryImpl
├── di/               # KoinModules.kt
├── domain/
│   ├── model/        # Joke
│   ├── repository/   # JokeRepository (interface only)
│   └── use_case/     # 7 use cases
├── presentation/
│   ├── navigation/   # AppNavHost, Screen, bottom nav components
│   ├── screens/      # 4 screens: home, ten-jokes, joke-details, my-favourites
│   └── ui/theme/     # Compose theme, colors, typography, shapes
├── ChiApplication.kt
└── MainActivity.kt
```

## Features implemented

The four screens cover the full architectural pipeline:

* **Random Joke** — fetches one joke from the network; tappable heart icon persists the joke to Room as a favourite. The favourite state is reactively observed from the database via Flow.
* **Ten Jokes** — fetches a list of ten jokes; tappable items navigate to the detail screen with the joke ID passed as a nav argument.
* **Joke Details** — fetches a joke by ID using `SavedStateHandle` to retrieve the nav argument; supports liking from the detail view as well.
* **My Favourite Jokes** — observes the Room database via Flow and displays all liked jokes; supports clearing all favourites.

Bottom navigation, nested navigation graphs, multiple back stacks, and dark/light theme toggle are all implemented.

## Testing strategy

Phase 4 expanded the project from sample-level testing into a meaningful regression safety net before modularization.

The current test suite covers:

- **Use cases** — all seven use cases are tested, including happy paths and error paths where relevant.
- **Remote `Resource` flows** — `GetJokeUseCase`, `GetTenJokesUseCase`, and `GetJokeByIdUseCase` verify Loading → Success, HTTP error, and network error branches.
- **Local/delegating use cases** — liked-jokes observation, liked-status observation, upsert, and delete-all behavior are tested for delegation and error propagation.
- **Mappers** — DTO/entity/domain mapping is tested explicitly, including preservation of `isFavourite`.
- **Repository implementation** — `JokeRepositoryImpl` is tested against mocked API and DAO dependencies to verify mapping, delegation, and exception propagation.
- **ViewModels** — all four screen ViewModels are tested with Turbine and a shared fake repository to verify StateFlow behavior.
- **Room DAO** — instrumented tests use an in-memory Room database to verify insert, query, favourite filtering, liked-state lookup, and delete-all behavior.

Current totals:

- **47 JVM unit tests**
- **5 Room DAO instrumented tests**
- **52 tests total**

Generated template tests were removed and replaced with meaningful coverage.

A detailed testing report is available in `docs/tests/Testing.md`.

## Technologies checklist

| #  | Item                                                             | Status |
| -- | ---------------------------------------------------------------- | :----: |
| 1  | Kotlin                                                           |   ✅   |
| 2  | Clean Architecture (3 layers)                                    |   ✅   |
| 3  | MVVM                                                             |   ✅   |
| 4  | Jetpack Compose + Navigation (single Activity, no Fragments)     |   ✅   |
| 5  | REST API with OkHttp + Retrofit2                                 |   ✅   |
| 6  | Database caching with Room (favourites persisted)                |   ✅   |
| 7  | Use cases with dependency inversion                              |   ✅   |
| 8  | Kotlin Coroutines + Flow + StateFlow                             |   ✅   |
| 9  | Koin Dependency Injection                                        |   ✅   |
| 10 | Dependency management with Gradle Kotlin DSL + Version Catalogue |   ✅   |
| 11 | Kotlin 2.x + Compose modernization                               |   ✅   |
| 12 | Expanded unit tests across use cases, repository, mappers, VMs   |   ✅   |
| 13 | Turbine-based ViewModel StateFlow tests                          |   ✅   |
| 14 | Room DAO in-memory instrumented tests                            |   ✅   |
| 15 | Three-module modularization (`:domain`, `:data`, `:app`)         |   ⏳   |
| 16 | Static analysis with ktlint / Spotless / Detekt / Konsist        |   ⏳   |
| 17 | Offline-first repository pattern                                 |   ⏳   |
| 18 | MockWebServer API integration tests                              |   ⏳   |
| 19 | Network connectivity monitoring                                  |   ⏳   |
| 20 | DataStore                                                        |   ⏳   |
| 21 | Kotlin Multiplatform exploration                                 |   ⏳   |

## Roadmap

The repository is modernized incrementally so each phase leaves the app buildable, reviewable, and easier to evolve. The current order is intentional: tests were expanded before modularization so future structural changes have regression protection.

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

Planned:

- **Three-module modularization**
    - Split the current single app module into `:domain`, `:data`, and `:app`.
    - Preserve the same Clean Architecture boundaries while making them enforceable at the Gradle module level.
    - Prepare the structure for future KMP source-set separation.

- **Static analysis and architecture enforcement**
    - Add ktlint, Spotless, Detekt, and Konsist.
    - Enforce formatting, complexity limits, and Clean Architecture dependency rules.

- **Offline-first repository pattern**
    - Move repository behavior toward cached-first flows backed by Room.
    - Emit cached data first, then refresh from the network and persist updates.

- **Kotlin Multiplatform**
    - Evaluate moving domain and data foundations toward shared Kotlin code.
    - Prepare for Room KMP, Ktor networking, and shared dependency wiring.

## Considered

The following are intentionally not included to keep the architectural focus clear:

* Firebase (FCM, Analytics, Crashlytics)
* Multiple unrelated features (the project remains single-feature by design)

## About

Created by Ramy ASSAF, Senior Mobile Engineer with a focus on Android architecture, modernization, and maintainability of mobile projects.

This repository is a deliberate teaching artifact: a compact, single-feature codebase used to demonstrate how Clean Architecture and SOLID principles translate into idiomatic modern Android. The scope is intentionally narrow so the architectural decisions remain legible.

## Resources

- [The Clean Architecture — Robert C. Martin (Uncle Bob)](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [Guide to app architecture — Android Developers](https://developer.android.com/topic/architecture)
- [Domain layer (optional) — Android Developers](https://developer.android.com/topic/architecture/domain-layer)
- [Testing a Flow — Android Developers](https://developer.android.com/kotlin/flow/test)

#