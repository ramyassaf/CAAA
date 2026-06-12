// Shared module configuration lives in build-logic convention plugins. The
// third-party plugins stay declared here with `apply false` so a single copy
// of each is on the main build classpath — the convention plugins compile
// against these APIs (compileOnly) and apply them by id at runtime.
plugins {
    id("com.compose.chi.module-architecture")
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.devToolsKsp) apply false
    alias(libs.plugins.kotlinJvm) apply false
}
