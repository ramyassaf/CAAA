package com.compose.chi.buildlogic

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * Convention plugin for Compose-enabled Android modules
 * (`com.compose.chi.android.compose`).
 *
 * Compose is a capability layered on top of a module archetype rather than an
 * archetype of its own, so this plugin composes with either Android convention
 * plugin instead of duplicating one per archetype. It owns the full Compose
 * toolchain:
 *
 * - the Compose compiler Gradle plugin (Kotlin and Compose compiler versions
 *   move in lockstep),
 * - the `compose` build feature,
 * - the Compose BOM, so modules declare Compose artifacts without versions,
 * - preview tooling (`ui-tooling-preview` to compile `@Preview`, with the
 *   full `ui-tooling` only on debug builds).
 *
 * Which Compose libraries a module actually uses (material3, navigation, …)
 * remains the module's own declaration.
 */
class AndroidComposeConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            // Fail fast with the fix spelled out: this plugin extends an
            // Android module and cannot stand alone.
            val android = extensions.findByType(CommonExtension::class.java)
                ?: throw GradleException(
                    "com.compose.chi.android.compose must be applied after an Android " +
                        "convention plugin (com.compose.chi.android.application or " +
                        "com.compose.chi.android.library) on project '$path'."
                )

            pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

            android.buildFeatures.compose = true

            dependencies {
                "implementation"(platform(libs.library("androidx-compose-bom")))
                "implementation"(libs.library("androidx-ui-tooling-preview"))
                "debugImplementation"(libs.library("androidx-ui-tooling"))
            }
        }
    }
}
