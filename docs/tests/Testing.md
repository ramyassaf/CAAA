# Phase 4 — Tests

## Goal

Build a meaningful test safety net for CAAA before the modularization
step. Lock down the current behavior at the use-case, repository, mapper,
ViewModel, and DAO layers so structural refactors (and the planned three-module
split) cannot silently change observable behavior.

## Stack used by the new tests

| Concern                       | Library                                       | Where                  |
|-------------------------------|-----------------------------------------------|------------------------|
| Test runner                   | JUnit 4.13.2                                  | `testImplementation`   |
| Mocking                       | MockK 1.14.9                                  | `testImplementation`   |
| Coroutines test scope         | `kotlinx-coroutines-test` 1.11.0              | `testImplementation`   |
| Flow / StateFlow assertions   | Turbine 1.2.1 (**added in Task 1**)           | `testImplementation`   |
| Android instrumented runner   | `androidx.test.ext:junit` 1.3.0               | `androidTestImplementation` |
| In-memory Room                | `androidx.room:room-ktx` (production dep)     | shared                 |

No Mockito, no Robolectric, no `room-testing`. Turbine is the only new library
introduced.

## Test layout

```
app/src/test/java/com/compose/chi/
├── testing/                                    # shared JVM helpers (Task 1)
│   ├── MainDispatcherRule.kt                   # JUnit4 TestWatcher swapping Dispatchers.Main
│   ├── TestJokes.kt                            # reusable Joke/JokeDto/JokeEntity fixtures
│   └── FakeJokeRepository.kt                   # hand-written JokeRepository fake
├── data/
│   ├── remote/dto/JokeDtoMapperTest.kt         
│   ├── database/model/JokeEntityMapperTest.kt  
│   └── repository/JokeRepositoryImplTest.kt    
├── domain/use_case/                            
│   ├── GetJokeUseCaseTest.kt
│   ├── GetTenJokesUseCaseTest.kt
│   ├── GetJokeByIdUseCaseTest.kt               
│   ├── ObserveLikedJokesUseCaseTest.kt
│   ├── ObserveJokeLikedStatusUseCaseTest.kt
│   ├── UpsertJokeUseCaseTest.kt
│   └── DeleteAllJokesUseCaseTest.kt
└── presentation/screens/                       
    ├── joke_home_page/JokeHomeViewModelTest.kt
    ├── joke_details_page/JokeDetailsViewModelTest.kt
    ├── ten_jokes_page/TenJokesViewModelTest.kt
    └── my_favourite_jokes_page/MyFavouriteJokesViewModelTest.kt

app/src/androidTest/java/com/compose/chi/
└── data/database/JokeDaoTest.kt                
```

Removed:

- `app/src/test/java/com/compose/chi/ExampleUnitTest.kt` (template `2 + 2 = 4`)
- `app/src/androidTest/java/com/compose/chi/ExampleInstrumentedTest.kt` (template `useAppContext`)


## Note: DAO test uses `runBlocking`

The plan was to use `runTest` from `kotlinx-coroutines-test`. On device,
`runTest 1.11.0` crashes with
`NoSuchMethodError: runBlockingK$default` because the
`debugAndroidTestRuntimeClasspath` resolves `kotlinx-coroutines-core` to
**1.9.0**, not 1.11.0. The downgrade comes from `androidx.test.ext:junit`
1.3.0 / `androidx.test.espresso:espresso-core` 3.7.0 publishing a
`kotlinx-coroutines-bom:{strictly 1.9.0}` constraint via Gradle Module
Metadata. Modern Gradle does not let `force()`, `eachDependency.useVersion()`,
or another `strictly()` declaration override an upstream `strictly` —
strict-vs-strict either intersects (and picks the lowest) or fails.

Per the plan's own guardrail ("If instrumented test setup becomes
unexpectedly large, stop and report the blocker instead of adding multiple
dependencies"), `JokeDaoTest` uses `runBlocking` from `kotlinx-coroutines-core`
(already on the classpath, no extra dependency). For DAO tests there is no
virtual time, no dispatcher control, no flow timing — `runBlocking` is
functionally equivalent.

The JVM unit-test classpath is unaffected by this constraint (it does not
pull `androidx-junit` / `androidx-espresso-core`), so Tasks 1–4 continue to
use the standard `runTest` from `kotlinx-coroutines-test` 1.11.0.

## How to run

Local JVM unit tests (all 48 tests across 13 classes):

```powershell
.\gradlew.bat testDebugUnitTest
```

Room DAO instrumented tests (requires a running emulator or attached device):

```powershell
.\gradlew.bat connectedDebugAndroidTest
```

If no device is attached, fall back to:

```powershell
.\gradlew.bat assembleDebugAndroidTest   # at least verifies the test APK builds
.\gradlew.bat assembleDebug              # confirms the app still builds
```

## Acceptance criteria check

- [x] Every use case has meaningful tests.
- [x] Remote use cases verify loading / success / HTTP / network branches.
- [x] Local use cases verify delegation + error propagation.
- [x] Repository implementation tested against mocked API and DAO.
- [x] DTO / entity / domain mappers tested.
- [x] ViewModel StateFlow behavior tested with Turbine.
- [x] Meaningful Room DAO instrumented test exists, executed on emulator.
- [x] Generated example tests removed.
- [x] Test helpers centralized in `com.compose.chi.testing`.
- [x] No Mockito, no Robolectric, no `room-testing`.
- [x] `testDebugUnitTest` passes.
- [x] `assembleDebug` and `assembleDebugAndroidTest` pass.
- [x] `connectedDebugAndroidTest` passes on Pixel 10 API 36.1.
- [x] One focused commit per task; no commit created without explicit user approval.
