package com.compose.chi.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * Convention plugin for modules wired into Koin dependency injection
 * (`com.compose.chi.koin`).
 *
 * The DI framework is a cross-cutting platform decision, not a per-module
 * choice, so the Koin BOM and koin-android core land here once instead of
 * being repeated by every participating module (:app, :presentation, :data).
 * The BOM rides on the implementation classpath, which also versions any
 * additional Koin artifact a module declares for itself — :presentation only
 * adds koin-androidx-compose, version-free.
 *
 * `:domain` never applies this plugin: the domain layer stays DI-framework
 * free by architecture policy (see `docs/modularization.md`).
 */
class KoinConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            dependencies {
                "implementation"(platform(libs.library("koin-bom")))
                "implementation"(libs.library("koin-android"))
            }
        }
    }
}
