// Top-level build file where you can add configuration options common to all subprojects/modules.
plugins {
    id("com.compose.chi.module-architecture")
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.devToolsKsp) apply false
    alias(libs.plugins.kotlinJvm) apply false
}
