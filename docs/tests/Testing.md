# Testing

Notes on how the test suite is laid out, what each layer protects, and what to reach for when adding more.

CAAA currently has:

- **81 JVM unit tests** across `:domain`, `:data`, and `:app`.
- **5 Room DAO instrumented tests** in `:data`.
- **86 tests total**.

JVM tests run with `./gradlew test`. The Room DAO tests require an emulator or device and run from `:data` with `./gradlew :data:connectedDebugAndroidTest`.

## Layers covered

- **Domain use cases** — thin delegation/orchestration over domain-safe `Resource` values.
- **Repository implementation** — translates API and DAO outcomes into `Resource<DomainError>` values. No framework exception leaks past this boundary except `CancellationException`, which is rethrown.
- **Mappers** — DTO → domain and entity ↔ domain mapping.
- **ViewModels** — StateFlow emissions in response to use-case results.
- **Architecture** — Konsist rules over module boundaries, package ownership, naming, and app/data separation.
- **Room DAO** — instrumented tests against an in-memory database.

## Tooling

- JUnit 4
- MockK
- Turbine for Flow and StateFlow assertions
- `kotlinx-coroutines-test` for JVM coroutine tests
- Konsist for architecture checks
- Gradle test fixtures for shared test helpers

## Layout

```text
domain/src/test/java/com/compose/chi/
├── architecture/                  Domain and use-case architecture rules
└── domain/use_case/               One file per use case

domain/src/testFixtures/java/com/compose/chi/testing/
├── TestJokes.kt                   Canonical domain Joke samples
└── FakeJokeRepository.kt          Pure-domain fake repository

data/src/test/java/com/compose/chi/
├── architecture/                  Data-layer architecture rules
└── data/
    ├── database/model/            Entity ↔ domain mapper tests
    ├── remote/dto/                DTO → domain mapper tests
    └── repository/                JokeRepositoryImpl behavior tests

data/src/testFixtures/java/com/compose/chi/testing/
├── JokeDtos.kt                    DTO factories
└── JokeEntities.kt                Entity factories

data/src/androidTest/java/com/compose/chi/data/database/
└── JokeDaoTest.kt                 In-memory Room DAO tests

app/src/test/java/com/compose/chi/
├── architecture/                  App-layer and project-wide architecture rules
├── presentation/screens/<feature>/  Per-screen ViewModel tests
└── testing/                       MainDispatcherRule
```

## The result type

The domain layer expresses success and failure through one shape:

```kotlin
sealed interface Resource<out T> {
    data class Success<T>(val data: T) : Resource<T>
    data class Error(val error: DomainError) : Resource<Nothing>
}

sealed interface DomainError {
    data object Network : DomainError
    data object NotFound : DomainError
    data object Server : DomainError
    data object Persistence : DomainError
    data object Unknown : DomainError
}
```

`JokeRepositoryImpl` is the single place that catches framework exceptions and maps them:

| Source | Maps to |
|---|---|
| `IOException` | `DomainError.Network` |
| `HttpException` 404 | `DomainError.NotFound` |
| `HttpException` 5xx | `DomainError.Server` |
| `HttpException` other | `DomainError.Unknown` |
| Any other `RuntimeException` | `DomainError.Unknown` |
| Any DAO failure, suspend or Flow | `DomainError.Persistence` |
| `CancellationException` | Rethrown unchanged |

Because the repository owns this mapping, use cases remain thin and their tests stay short by design.

## Shared helpers

`MainDispatcherRule` lives in `:app` test sources because it is used by ViewModel tests.

Domain-level fixtures live in `:domain` test fixtures:

- `TestJokes` defines canonical `Joke` samples and builders.
- `FakeJokeRepository` is the hand-written `JokeRepository` used by ViewModel and use-case tests. Its methods have configurable `Resource` return values; liked jokes and per-id liked status are backed by `MutableStateFlow` so tests can drive emissions.

Data-specific fixtures live in `:data` test fixtures:

- `JokeDtos` defines DTO samples.
- `JokeEntities` defines Room entity samples.

This keeps fixture ownership aligned with the production model ownership and avoids duplicating the same helper across modules.

Default to the fake repository for ViewModel tests. Reach for MockK when a test specifically needs to verify dependency calls or simulate framework/API/DAO behavior.

## What each layer protects

### Use cases

Use cases are tested in `:domain`. They verify pass-through/delegation behavior over the `JokeRepository` contract and domain-safe `Resource` values. The interesting technical branches live below them in `:data`.

### Repository

`JokeRepositoryImplTest` is the largest test file. It pins the data boundary behavior:

- `Resource.Success` carries mapped domain objects on the happy path.
- `IOException` maps to `Network`.
- `HttpException` 404 maps to `NotFound`.
- `HttpException` 5xx maps to `Server`.
- Other HTTP/runtime failures map to `Unknown`.
- DAO failures, suspend or Flow, map to `Persistence`.
- `CancellationException` is rethrown from every relevant path.

It also verifies mapper usage and preservation of `isFavourite` through Room-backed flows.

### Mappers

Mapper tests are deliberately simple and explicit:

- `JokeDtoMapperTest` — every field carried, `isFavourite` defaults to `false`.
- `JokeEntityMapperTest` — both directions, every field including `isFavourite`.

The entity test protects against a previous regression where `JokeEntity.toJoke()` silently dropped `isFavourite`.

### ViewModels

ViewModel tests live in `:app` and drive real use cases backed by `FakeJokeRepository`. Turbine handles StateFlow assertions.

Useful rules when adding more:

- Pass `mainDispatcherRule.testDispatcher` into `runTest(...)`.
- The ViewModels start work in `init`; collect `state` before advancing the dispatcher when asserting the first emission.
- `MyFavouriteJokesViewModel.state` uses `stateIn(... WhileSubscribed(5000), ...)`; assert it through an active Turbine subscription instead of relying on a stale `state.value` read.

### Architecture (Konsist)

Konsist tests are split by ownership:

- `:domain` owns domain purity and use-case shape rules.
- `:data` owns data-layer placement, DTO/entity/API/repository implementation rules.
- `:app` owns app-layer and project-wide rules, including the composition-root rule that only `ChiApplication` and the app DI package may import `com.compose.chi.data.*`.

These tests complement Gradle module boundaries. Gradle prevents impossible module arrows, while Konsist documents and verifies the finer app-internal rules.

### Room DAO (instrumented)

`JokeDaoTest` lives in `:data/src/androidTest` and uses `Room.inMemoryDatabaseBuilder(...)`. Five cases are covered:

- Insert and read round-trip.
- `observeAllLikedJokes()` filters down to favourites.
- `observeFavoriteJoke(id)` returns true / false / false for liked, not-liked, and missing ids.
- `deleteAllJokes()` clears the table.

This file uses `runBlocking` rather than `runTest` because the Android test classpath currently resolves `kotlinx-coroutines-core` to an older version through AndroidX test dependencies, which makes `runTest` crash at runtime. DAO tests do not need virtual time, dispatcher control, or Flow scheduling, so `runBlocking` is sufficient here.

## Running

Run all JVM tests and architecture checks:

```bash
./gradlew test
```

Run a specific module's JVM tests:

```bash
./gradlew :domain:test
./gradlew :data:test
./gradlew :app:test
```

Run the DAO instrumented tests, with an emulator or attached device:

```bash
./gradlew :data:connectedDebugAndroidTest
```

Build the app:

```bash
./gradlew assembleDebug
```

CI currently runs:

```bash
./gradlew test
./gradlew assembleDebug
```
