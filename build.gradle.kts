// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    dependencies {
        // Gradle Plugin Version
        classpath(libs.gradle)
    }
}

plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.jetbrainsKotlin) apply false
    alias(libs.plugins.devToolsKsp) apply false
}

tasks.register("clean", Delete::class) {
    delete(layout.buildDirectory)
}