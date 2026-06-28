package com.compose.chi.buildlogic

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/**
 * Convention plugin for Android library modules
 * (`com.compose.chi.android.library`).
 *
 * Applies the Android library plugin plus the project-wide Android
 * conventions from [configureKotlinAndroid]. A library module's own build
 * script is left with what genuinely distinguishes it: its namespace, its
 * dependencies, and opt-in features such as test fixtures.
 *
 * No target SDK here on purpose — it is meaningless for libraries and AGP
 * deprecated it; only the application sets one.
 */
class AndroidLibraryConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.android.library")

            extensions.configure<LibraryExtension> {
                configureKotlinAndroid(this)
            }

            configureUniversalTestDependencies()
        }
    }
}
