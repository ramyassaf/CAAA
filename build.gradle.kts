// Top-level build file where you can add configuration options common to all subprojects/modules.
buildscript {
    dependencies {
        // AGP 9 bundles KGP 2.2.10 and KSP 2.2.10-2.0.2. Override here to use newer versions.
        // See: https://developer.android.com/build/releases/agp-9-0-0-release-notes#kotlin
        classpath(libs.kotlin.gradle.plugin)
        classpath(libs.ksp.gradle.plugin)
    }
}

plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.devToolsKsp) apply false
}

tasks.register("clean", Delete::class) {
    delete(layout.buildDirectory)
}