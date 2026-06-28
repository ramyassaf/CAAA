package com.compose.chi.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

/**
 * Convention plugin for pure Kotlin/JVM modules
 * (`com.compose.chi.kotlin.jvm`).
 *
 * Applies the Kotlin JVM plugin and pins the JVM toolchain to the same Java
 * version the Android modules compile against, read from the version catalog.
 * `:domain` is the archetype consumer: business logic with no Android
 * dependency, kept ready for non-Android reuse.
 */
class KotlinJvmConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jetbrains.kotlin.jvm")

            extensions.configure<KotlinJvmProjectExtension> {
                jvmToolchain(libs.version("java").toInt())
            }

            configureUniversalTestDependencies()
        }
    }
}
