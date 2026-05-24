# Testing

Notes on how the test suite is laid out, what each layer is protecting, and what
to reach for when adding more.

JVM unit tests run under `testDebugUnitTest`. The Room DAO test runs on an
emulator or device under `connectedDebugAndroidTest`.

## Layers covered

- **Mappers** — DTO → domain, entity ↔ domain.
- **Use cases** — pass-through on success, error semantics on failure.
- **Repository** — translates API and DAO outcomes into
  `Resource<DomainError>`. No framework exception leaks past this boundary,
  except `CancellationException`, which is rethrown.
- **ViewModels** — StateFlow emissions in response to use-case results.
- **Architecture** — Konsist rules over package boundaries and naming.
- **Room DAO** — one instrumented test against an in-memory database.

## Tooling

- JUnit 4
- MockK
- Turbine for any Flow or StateFlow assertion
- `kotlinx-coroutines-test` for `runTest` and virtual time
- Konsist for the architecture checks

## Layout

```
app/src/test/java/com/compose/chi
├── architecture/                  Konsist rules over the codebase
├── data/
│   ├── database/model/            entity ↔ domain mapper
│   ├── remote/dto/                DTO → domain mapper
│   └── repository/                JokeRepositoryImpl behaviour
├── domain/use_case/               one file per use case
├── presentation/screens/<feature>/  per-screen ViewModel tests
└── testing/                       shared rule + fixtures + fake
    ├── MainDispatcherRule.kt
    ├── TestJokes.kt
    └── FakeJokeRepository.kt

app/src/androidTest/java/com/compose/chi/data/database/
└── JokeDaoTest.kt
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

`JokeRepositoryImpl` is the single place that catches framework exceptions and
maps them:

| Source                                | Maps to                  |
|---------------------------------------|--------------------------|
| `IOException`                         | `DomainError.Network`    |
| `HttpException` 404                   | `DomainError.NotFound`   |
| `HttpException` 5xx                   | `DomainError.Server`     |
| `HttpException` other                 | `DomainError.Unknown`    |
| Any other `RuntimeException`          | `DomainError.Unknown`    |
| Any DAO failure (suspend or Flow)     | `DomainError.Persistence`|
| `CancellationException`               | rethrown unchanged       |

Because the repository owns this mapping, use cases on top are thin
pass-throughs and their tests are short by design.

## Shared helpers

Everything under `testing/` is reused across the suite.

`MainDispatcherRule` is a `TestWatcher` that swaps `Dispatchers.Main` for a
`StandardTestDispatcher` per test. ViewModel tests pass
`mainDispatcherRule.testDispatcher` into `runTest(...)` so the test scope and
`viewModelScope` share a scheduler — `advanceUntilIdle()` then controls both.

`TestJokes` is the only place fixture data is defined: canonical `Joke`,
`JokeDto`, and `JokeEntity` instances (favourite and non-favourite variants,
plus a `tenJokes()` builder). Use it; do not inline data.

`FakeJokeRepository` is the hand-written `JokeRepository` used by ViewModel and
use-case tests. Each method has a configurable `Resource` return value; liked
jokes and per-id liked status are backed by `MutableStateFlow` so tests can
drive emissions. There are also a few recorders — `upsertedJokes`,
`deleteAllCallCount`, `lastRequestedJokeId` — for assertions about *what was
called*, not just *what came back*.

Default to the fake. Reach for MockK when a test specifically needs to verify
the arguments a dependency was called with.

## What each layer protects

### Mappers

Two files, deliberately boring.

- `JokeDtoMapperTest` — every field carried, `isFavourite` defaults to `false`.
- `JokeEntityMapperTest` — both directions, every field including
  `isFavourite`.

The entity test is here because an earlier version of `JokeEntity.toJoke()`
silently dropped `isFavourite`, so liked jokes loaded from Room arrived in the
UI with the flag cleared. This test exists to keep that regression dead.

### Use cases

Pass-through behaviour. The observing use cases have two tests apiece (success,
error). The remote suspend use cases have one each — `assertSame` on the
repository result plus a `coVerify` that the repository was called. The
interesting branches live in the repository.

### Repository

The largest test file. Every branch of the error mapping is pinned:

- `Resource.Success` carries the mapped domain object on the happy path.
- `IOException` → `Network`.
- `HttpException` 404 → `NotFound`, 5xx → `Server`, anything else → `Unknown`.
- Random `RuntimeException` → `Unknown`.
- DAO failures, suspend or Flow, → `Persistence`.
- `CancellationException` is rethrown from every path.

Plus the structural checks: `observeLikedJokes` preserves `isFavourite = true`
on every emitted joke (repository-layer guard for the mapper above); `upsertJoke`
builds a `JokeEntity` with every field, including `isFavourite`, and delegates
to the DAO exactly once.

### ViewModels

Tests drive real use cases through `FakeJokeRepository`. Turbine handles every
StateFlow assertion. A few things to know before adding more:

- Pass `mainDispatcherRule.testDispatcher` into `runTest(...)`.
- The ViewModels start work in `init`. Collect `state` before advancing the
  dispatcher when asserting the initial emission, otherwise the first item
  will already be the loading state.
- `MyFavouriteJokesViewModel.state` is built with
  `stateIn(... WhileSubscribed(5000), ...)`. Reading `state.value` without an
  active subscription returns the stale initial default. Wrap reads in
  `state.test { ... }` or skip past the first emission explicitly.

One quirk worth knowing: `MyFavouriteJokesViewModel.isLoading` is permanently
`false` in the current production code (the initial value defaults to `false`
and the `combine` block also produces `false`). The test asserts that
behaviour; the day it changes, the VM and the test need to move together.

### Architecture (Konsist)

A small set of rules that catch trivial drift before review reaches it:

- No wildcard imports anywhere; no empty Kotlin files.
- No `TODO` strings in production sources.
- `*Repository` interfaces live under `domain/repository/`, `*RepositoryImpl`
  classes under `data/repository/`, and the impl actually implements a domain
  interface.
- Equivalent boundary rules for use cases, the remote API, and the
  domain ↔ data split.

Cheap to run, stable, and they make the package layout self-enforcing.

### Room DAO (instrumented)

`JokeDaoTest` uses `Room.inMemoryDatabaseBuilder(...)`, closes the database in
`@After`, and reads flows with `Flow.first()`. Five cases:

- Insert and read round-trip.
- `observeAllLikedJokes()` filters down to favourites.
- `observeFavoriteJoke(id)` returns true / false / false for liked,
  not-liked, and missing ids.
- `deleteAllJokes()` clears the table.

This file uses `runBlocking` rather than `runTest`. Worth the paragraph because
it looks inconsistent at first glance:

> `runTest 1.11.0` crashes on device with
> `NoSuchMethodError: runBlockingK$default`. The
> `debugAndroidTestRuntimeClasspath` resolves `kotlinx-coroutines-core` to
> 1.9.0 because `androidx.test.ext:junit` 1.3.0 and
> `androidx.test.espresso:espresso-core` 3.7.0 publish a
> `kotlinx-coroutines-bom:{strictly 1.9.0}` constraint via Gradle Module
> Metadata. Modern Gradle does not let `force()`,
> `eachDependency.useVersion()`, or another `strictly()` override an upstream
> `strictly` — strict-vs-strict either intersects (lowest wins) or fails the
> resolution.

DAO tests don't need virtual time, dispatcher control, or Flow scheduling, so
`runBlocking` is fine here. The JVM classpath doesn't pull the Android test
artifacts, so everything else stays on `runTest`.

## Running

JVM unit tests (mappers, use cases, repository, ViewModels, architecture):

```
./gradlew testDebugUnitTest
```

Instrumented DAO test, requires an emulator or attached device:

```
./gradlew connectedDebugAndroidTest
```

If no device is attached, `assembleDebugAndroidTest` will at least verify the
test APK builds, and `assembleDebug` confirms the app itself still builds.

### CI verification

GitHub Actions runs the core verification workflow on pull requests and pushes targeting `dev` and `main`, and can also be triggered manually.

CI currently runs:

```
./gradlew test
./gradlew assembleDebug
```

This verifies the JVM test suite, Konsist architecture tests, and debug build assembly.

Instrumented Room DAO tests remain manual/emulator-based verification through:

```
./gradlew connectedDebugAndroidTest
```

## When adding a test

- Use `TestJokes` for fixture data. If the fixtures you need aren't there,
  add them to `TestJokes` rather than inlining.
- Drive ViewModel and use-case tests through `FakeJokeRepository`. Reach for
  MockK when you need to assert on *call sites and arguments*.
- New `DomainError` cases must flow through `Resource.Error(DomainError.*)`;
  no exception types should leak past the repository.
- New repositories and use cases need entries in the Konsist rules if the
  default name-pattern checks don't cover them yet.
