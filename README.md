# CAAA — Clean Architecture Jetpack Compose Android Skeleton

> **Status:** Work in progress. The repository is actively maintained; new features and architectural improvements are added incrementally. See [Roadmap](#roadmap) below.

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
- **Use cases are minimal and single-purpose.** Each use case exposes exactly one `operator fun invoke` and orchestrates a single piece of business logic. The codebase currently includes seven: `GetJokeUseCase`, `GetTenJokesUseCase`, `GetJokeByIdUseCase`, `GetLikedJokesUseCase`, `IsJokeLikedUseCase`, `UpsertJokeUseCase`, `DeleteAllJokesUseCase`.
- **Manual DI is intentional.** The `AppModule` interface plus `AppModuleImpl` implementation, accessed through `ChiApplication.appModule`, exists to make the dependency-inversion principle visible to readers. The same wiring would normally be done with Hilt or Koin in a production app; the manual approach is a demonstration choice.

## Tech stack

| Concern | Choice |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose, Material 3 |
| Navigation | Navigation Compose (single Activity, no Fragments) |
| Async | Kotlin Coroutines, Flow, StateFlow |
| Local storage | Room |
| Networking | Retrofit 2, OkHttp, Gson |
| DI | Manual (`AppModule` interface + impl, accessed via `ChiApplication`) |
| Testing | JUnit 4, MockK, `kotlinx-coroutines-test` |
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
├── di/               # AppModule interface + AppModuleImpl
├── domain/
│   ├── model/        # Joke
│   ├── repository/   # JokeRepository (interface only)
│   └── use_case/     # 7 use cases
├── presentation/
│   ├── helpers/      # ViewModelFactoryHelper
│   ├── navigation/   # AppNavHost, Screen, bottom nav components
│   ├── screens/      # 4 screens: home, ten-jokes, joke-details, my-favourites
│   └── ui/theme/     # Compose theme, colors, typography, shapes
├── ChiApplication.kt
└── MainActivity.kt
```

## Features implemented

The four screens cover the full architectural pipeline:

- **Random Joke** — fetches one joke from the network; tappable heart icon persists the joke to Room as a favourite. The favourite state is reactively observed from the database via Flow.
- **Ten Jokes** — fetches a list of ten jokes; tappable items navigate to the detail screen with the joke ID passed as a nav argument.
- **Joke Details** — fetches a joke by ID using `SavedStateHandle` to retrieve the nav argument; supports liking from the detail view as well.
- **My Favourite Jokes** — observes the Room database via Flow and displays all liked jokes; supports clearing all favourites.

Bottom navigation, nested navigation graphs, multiple back stacks, and dark/light theme toggle are all implemented.

## Technologies checklist

| # | Item | Status |
|---|---|:---:|
| 1 | Kotlin | ✅ |
| 2 | Clean Architecture (3 layers) | ✅ |
| 3 | MVVM | ✅ |
| 4 | Jetpack Compose + Navigation (single Activity, no Fragments) | ✅ |
| 5 | REST API with OkHttp + Retrofit2 | ✅ |
| 6 | Database caching with Room (favourites persisted) | ✅ |
| 7 | Use cases (Dependency Inversion impl for unit testing) | ✅ |
| 8 | Kotlin Coroutines + Flow + StateFlow | ✅ |
| 9 | Manual Dependency Injection | ✅ |
| 10 | Dependency management with Gradle Kotlin DSL + Version Catalogue | ✅ |
| 11 | Unit Tests (sample) | ✅ |
| 12 | Network Connectivity monitoring | ⏳ |
| 13 | DataStore (replacement for SharedPreferences) | ⏳ |
| 14 | MockWebServer for repository/API integration tests | ⏳ |
| 15 | Offline-first repository pattern (cache + network) | ⏳ |
| 16 | Full unit test coverage across all use cases and ViewModels | ⏳ |

## Roadmap

The repository is actively maintained. Planned additions:

- **Expanded test coverage** — Tests for every use case (happy path + error paths), at least one ViewModel test using Turbine for Flow assertions, and a repository integration test using `Room.inMemoryDatabaseBuilder`.
- **Offline-first repository pattern** — Refactor remote use cases to emit cached data first, then fetch from network, persist, and re-emit the fresh value. This is the canonical Clean Architecture data-layer pattern and currently the most visible gap.
- **MockWebServer integration** — End-to-end tests of the data layer against a controllable fake HTTP server.
- **DataStore** — Replace any future SharedPreferences needs with DataStore (typed key-value or Proto).
- **Network connectivity monitoring** — Expose connectivity state as a Flow consumable by ViewModels for offline UX handling.

## Considered but out of scope

The following are intentionally not included to keep the architectural focus clear:

- Firebase (FCM, Analytics, Crashlytics)
- Multiple unrelated features (the project remains single-feature by design)
- Production-grade DI framework (Hilt/Koin) — the manual DI is a demonstration choice, see [Architectural decisions](#key-architectural-decisions)

## About

Created by Ramy ASSAF, Senior Mobile Engineer with a focus on Android architecture, modernization, and maintainability of mobile projects.

This repository is a deliberate teaching artifact: a compact, single-feature codebase used to demonstrate how Clean Architecture and SOLID principles translate into idiomatic modern Android. The scope is intentionally narrow so the architectural decisions remain legible.

## Resources

- [The Clean Architecture — Robert C. Martin (Uncle Bob)](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [Guide to app architecture — Android Developers](https://developer.android.com/topic/architecture)
- [Domain layer (optional) — Android Developers](https://developer.android.com/topic/architecture/domain-layer)
- [Testing a Flow — Android Developers](https://developer.android.com/kotlin/flow/test)

#