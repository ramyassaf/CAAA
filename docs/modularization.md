# Modularization

The app lives in four Gradle modules with a strict dependency direction:

```text
                ┌──────────┐
                │   :app   │           Android application
                └────┬─────┘
            ┌────────┴────────┐
            ▼                 ▼
    ┌───────────────┐   ┌──────────┐
    │ :presentation │   │  :data   │   Android libraries
    └───────┬───────┘   └────┬─────┘
            └────────┬────────┘
                     ▼
               ┌──────────┐
               │ :domain  │           pure Kotlin/JVM
               └──────────┘
```
That physical module graph should not be confused with the logical Clean Architecture layer graph:

```text
presentation ─────► domain ◄───── data
   UI + VM          use cases       repository impl
   UI state         contracts       Room / Retrofit
```

- `:app` depends on `:presentation` and `:data` because it is the Android application and composition root. 
- `:presentation` depends on `:domain` only.
- `:data` depends on `:domain`.
- `:domain` depends on no project modules.

Module dependency direction is verified by a Gradle architecture check, while Konsist remains focused on code-shape rules that the Gradle module graph does not express.

---

## Why modularizing

Before the split, CHI was a single `:app` module with Clean Architecture
boundaries at the *package* level (`presentation`, `domain`, `data`). That
arrangement is fine, but nothing enforces it physically. A `ViewModel` is one
auto-import away from reaching directly into `data.remote.JokeApi`, and the
build will happily compile it. The package boundary is a convention, not a
contract.

Moving to Gradle modules buys you three concrete things:

1. **Enforced architecture.** `:domain` literally cannot see Room, Retrofit,
   Koin, or Android — they're not on its classpath. The compiler becomes
   your reviewer.
2. **Clearer incremental build boundaries.** UI changes stay in
   `:presentation`, domain changes stay in `:domain`, and infrastructure
   changes stay in `:data`, giving Gradle better separation than a single
   large module.
3. **A reusable core.** `:domain` is pure Kotlin/JVM, so it is the natural
   candidate for a future Multiplatform extraction or other non-Android
   reuse with minimal architectural change.

If your app is small and stable, you may not need this. If it's growing,
multiple people touch it, or you ever want KMP, do it early.

---

## The four modules in detail

### `:domain` — the business core

Pure Kotlin/JVM. No Android. No Room. No Retrofit. No Koin. No DI
framework at all.

**Lives here:**

- Domain models (`Joke`).
- Repository contracts (`JokeRepository` — interfaces only).
- Use cases (`GetJokeUseCase`, `ObserveLikedJokesUseCase`, …).
- Result and error abstractions (`Resource`, `DomainError`).

**Allowed dependencies:**

- The Kotlin standard library.
- `kotlinx-coroutines-core` — because `Flow` appears in repository
  contracts. That's the only third-party dependency.
- Test dependencies (JUnit, MockK, Turbine, coroutines-test).

**Forbidden in production code:**

- `android.*`, `androidx.*` (including Room).
- `retrofit2.*`, `okhttp3.*`.
- `java.io.IOException`, `java.net.*`.
- Any DI framework API (Koin, Hilt, Dagger).
- DTOs, Room entities, DAOs, database classes.
- Concrete repository implementations.
- Anything from presentation.

The temptation will appear: "I just need `androidx.annotation.IntRange`
on this domain model." Resist it. Once one Android symbol leaks in, the
discipline is gone and the module starts pulling its full weight again.

### `:data` — the implementation details

An Android library module. Depends on `:domain`.

**Lives here:**

- Room database, DAOs, entities.
- Retrofit API interface and DTOs.
- Repository implementation (`JokeRepositoryImpl`).
- Technical exception mapping — `HttpException`, `IOException`, Room
  failures all get translated into `DomainError` here. **No framework
  exception ever crosses into `:domain` or `:app`.**
- Network configuration (`BASE_URL_JOKES` lives in a small `NetworkConfig`
  file, kept `internal` to the module).
- A Koin module that wires the data graph (`dataKoinModule`).

**Notable boundary rule:** `:data` does not depend on `:app` or
`:presentation`. That arrow is forbidden by Gradle.

### `:presentation` — the UI layer

An Android library module. Depends only on `:domain`.

**Lives here:**

- Compose screens, their UI state, and ViewModels.
- Navigation (`AppNavHost`, `Screen`, and bottom-navigation components).
- Theme (colors, typography, shapes, dimensions).
- UI error mapping (`DomainErrorUiMapper`) — turns `DomainError` into
  user-facing messages.
- The Compose entry point (`ChiPresentationRoot`), hosted by `MainActivity`.
- A Koin module that wires use cases and ViewModels (`presentationKoinModule`).

**Notable boundary rule:** `:presentation` depends only on `:domain`, never on
`:data`. The dependency direction is enforced by `verifyModuleArchitecture`.

### `:app` — the composition root

The Android application module. Depends on `:presentation` and `:data`.

**Lives here:**

- Application class (`ChiApplication`) — the composition root; starts Koin
  and installs `dataKoinModule` and `presentationKoinModule`.
- `MainActivity` — the Android entry point; hosts the Compose content from
  `:presentation`.
- Analytics and anything else that is app-specific.

**The composition rule:** `:app` can see `:data`, but only `ChiApplication`
may touch it — and only to install the data Koin module. Every other `:app`
file (such as `MainActivity` and analytics) imports nothing from
`com.compose.chi.data`. Two `AppLayerArchitectureTest` rules enforce this, not
just by convention.

---

## Step-by-step: how to do this on a real codebase

Order matters. Do it in this sequence and the build keeps compiling as
you go.

### 1. Set up the Gradle plumbing first

Before moving any source files, get the module shells in place.

**`settings.gradle.kts`:**

```kotlin
rootProject.name = "CHI"
include(":app")
include(":domain")
include(":data")
include(":presentation")
```

**Root `build.gradle.kts`** — use the plugins DSL with `apply false`,
not the legacy `buildscript { classpath(...) }` pattern. The catalog
controls every plugin version in one place:

```kotlin
plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.devToolsKsp) apply false
    alias(libs.plugins.kotlinJvm) apply false
}
```

The plugins-block alias has been the recommended Android pattern since
AGP 7 / Android Studio Arctic Fox (2021), and the `gradle/libs.versions.toml`
catalog became the Studio default around AGP 8. Use both. The old
`buildscript { classpath(libs.kotlin.gradle.plugin) }` shape still
works but mirrors plugins as library coordinates just to feed
`classpath()` — it's the legacy form.

**`domain/build.gradle.kts`** — pure Kotlin/JVM, no AGP:

```kotlin
plugins {
    alias(libs.plugins.kotlinJvm)
    `java-test-fixtures`   // see "Sharing test code" below
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    // test deps only
}
```

No separate `java { sourceCompatibility = … }` block needed —
`kotlin.jvmToolchain(17)` drives both Kotlin and Java compile targets.

**`data/build.gradle.kts`** — Android library, KSP for Room:

```kotlin
plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.devToolsKsp)
}

android {
    namespace = "com.compose.chi.data"
    compileSdk = 36
    defaultConfig { minSdk = 24 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    testFixtures { enable = true }
}

dependencies {
    implementation(project(":domain"))
    // Retrofit, OkHttp, Room, Koin-android, …
}
```

**`presentation/build.gradle.kts`** — Android library, Compose UI:

```kotlin
dependencies {
    implementation(project(":domain"))
    // Compose, navigation, lifecycle, Koin-android, koin-androidx-compose, …
}
```

**`app/build.gradle.kts`** — wires presentation and data at the composition root:

```kotlin
dependencies {
    implementation(project(":presentation"))
    implementation(project(":data"))
    // Koin-android, activity-compose, core-ktx, …
}
```

After this step the project still builds — there's just nothing in the
new modules yet.

### 2. Move sources, layer by layer

Move `:domain` first (it has the fewest dependencies), then `:data`
and `:presentation` (which both depend on `:domain`), then clean up
`:app` last.

```text
app/src/main/java/com/compose/chi/domain/**  →  domain/src/main/java/com/compose/chi/domain/**
app/src/main/java/com/compose/chi/data/**    →  data/src/main/java/com/compose/chi/data/**
```

**Keep package names stable.** `com.compose.chi.domain.model.Joke` stays
`com.compose.chi.domain.model.Joke` after the move.

### 3. Split dependency injection

The original single-module project had one
Koin module wiring everything. Split it along ownership lines:

**`dataKoinModule` (lives in `:data`):**

- `HttpLoggingInterceptor`, `OkHttpClient`, `Retrofit`, `JokeApi`.
- `AppDatabase`, `JokeDao`.
- `JokeRepository` binding to `JokeRepositoryImpl`.

**`presentationKoinModule` (lives in `:presentation`):**

- Use-case factories.
- ViewModel bindings.

**`ChiApplication`** loads both:

```kotlin
startKoin {
    androidContext(this@ChiApplication)
    modules(dataKoinModule, presentationKoinModule)
}
```

Two non-obvious rules to bake in:

- **No Koin in `:domain` production code.** Domain doesn't know how it
  gets wired up. If it ever needs DI metadata, you're putting business
  logic in the wrong place.
- **No `:data` → `:app` imports**, even via DI. `:data` exposes its own
  Koin module; `:app` consumes it. The dependency arrow stays correct.

### 4. Move tests to follow ownership

Each layer's tests move with the layer:

| Test type | Lives in |
|---|---|
| Use-case tests | `:domain/src/test/` |
| Mapper tests (DTO ↔ domain, entity ↔ domain) | `:data/src/test/` |
| Repository implementation tests | `:data/src/test/` |
| Room DAO instrumented tests | `:data/src/androidTest/` |
| ViewModel / presentation tests | `:presentation/src/test/` |

### 5. Share test fixtures the right way

ViewModel tests in `:presentation` need
`FakeJokeRepository` (which only knows `:domain` types). Use-case tests
in `:domain` want the same canonical `Joke` samples. Mapper tests in
`:data` want both domain `Joke` values and the corresponding DTOs.

Recommended approach:

**Use Gradle's `java-test-fixtures` plugin**. It is built for exactly this.

```text
domain/src/testFixtures/java/com/compose/chi/testing/
  TestJokes.kt         ← canonical Joke samples
  FakeJokeRepository.kt ← pure-domain fake of JokeRepository

data/src/testFixtures/java/com/compose/chi/testing/
  JokeDtos.kt          ← Dto factories, built from TestJokes
  JokeEntities.kt      ← Entity factories, built from TestJokes
```

Consumers wire it up explicitly:

```kotlin
// data/build.gradle.kts
testFixturesImplementation(testFixtures(project(":domain")))
testImplementation(testFixtures(project(":domain")))

// app/build.gradle.kts
testImplementation(testFixtures(project(":domain")))
```

The result: every fixture has a single canonical source, and consumers
opt in at the configuration level instead of through duplication.

### 6. Adapt architecture tests

If you use Konsist (or any source-scanning architecture-test tool),
**colocate the tests with the module they assert about.** That way
`./gradlew :domain:test` independently verifies `:domain` invariants
without depending on `:app`.

In CHI:

- `:domain` owns `DomainLayerArchitectureTest` + `UseCaseArchitectureTest`.
  Includes rules like "any interface ending in `Repository` lives in
  `domain.repository`" and "domain must not contain any `*Api` types."
- `:data` owns `DataLayerArchitectureTest`. Includes "DTOs live in
  `data.remote.dto`", "Retrofit annotations stay confined to
  `data.remote`", "RepositoryImpl declarations implement a
  `domain.repository` contract."
- `:app` owns `ProjectArchitectureTest` (project-wide hygiene: no
  wildcard imports, no production TODOs, no empty files) and
  `AppLayerArchitectureTest`.

The root `verifyModuleArchitecture` task owns module dependency direction:
`:domain` has no production project dependencies, `:data` and `:presentation`
depend only on `:domain`, and `:app` depends only on `:presentation` and
`:data`.

Encoding this contract as a Gradle plugin keeps it executable rather than
aspirational: the module graph is verified on every local and CI build, so a
dependency that breaks the intended direction fails immediately instead of
slipping through review. It also gives the architecture one authoritative
definition that travels with the code as the project grows.

`AppLayerArchitectureTest` is the one to copy into your project even if
you skip the others. It encodes the composition-root rule:

```kotlin
@Test
fun `only ChiApplication may import the data layer`() {
    Konsist.scopeFromProject()
        .files
        .withPath("..app/src/main/java/com/compose/chi..")
        .withoutPath("..app/src/main/java/com/compose/chi/ChiApplication.kt")
        .imports.assertFalse { import ->
            import.name.startsWith("com.compose.chi.data.")
        }
}
```

This catches the auto-import slip in `:app`: a file such as `MainActivity`
can't silently `import com.compose.chi.data.remote.JokeApi`; CI fails with an
architecture violation pointing straight at the file.

A second rule narrows the one allowed exception — the composition root may
reach into `:data` for the Koin module and nothing else:

```kotlin
@Test
fun `ChiApplication may import only the data Koin module from data`() {
    Konsist.scopeFromProject()
        .files
        .withPath("..app/src/main/java/com/compose/chi/ChiApplication.kt")
        .imports.assertFalse { import ->
            import.name.startsWith("com.compose.chi.data.") &&
                import.name != "com.compose.chi.data.di.dataKoinModule"
        }
}
```

Together the two rules keep the `:data` surface in `:app` as small as it can
be: one file, one import.

---

## Verification

Two layers, both required:

**Automated:**

```bash
./gradlew clean
./gradlew verifyModuleArchitecture
./gradlew test           # all module unit tests + architecture tests
./gradlew assembleDebug
./gradlew connectedDebugAndroidTest   # DAO tests, needs device/emulator
```

**Manual** — open the app and exercise every screen. Automated tests
catch contract violations; manual catches the things contracts don't
encode (loading states, navigation timing, error messaging).

---

### Things that go wrong if you're not careful

- **Putting Koin in `:domain`.** Tempting when a use case needs config.
  Don't. Use constructor parameters; configure them at the composition
  root.
- **Leaving every Konsist test in `:app`.** Works (Konsist scans the
  whole project filesystem) but conceptually wrong — and means
  `:domain:test` can't independently verify `:domain` invariants.
- **Module-level `build/` directories not gitignored.** If your
  `.gitignore` has `/build` (anchored), it only excludes the root. Add
  `build/` (non-anchored) for the new module directories.

---


## What you get at the end

- A build where `:domain` literally cannot import Android.
- A Konsist guardrail where presentation cannot import `:data`.
- Test commands that scope per module: `:domain:test` checks domain.
- Architecture tests living with their owners.
- One canonical copy of every test fixture.
- A composition root that's the only place concrete implementations
  meet abstract contracts.

None of this is novel. It is disciplined application of Clean
Architecture: Gradle module graph enforcement protects the inter-module arrows, and Konsist closes
the remaining app-internal gap created by the composition root. That
discipline is what makes the next ten features easier to add.
