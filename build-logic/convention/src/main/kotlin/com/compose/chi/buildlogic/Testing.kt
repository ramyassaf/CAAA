package com.compose.chi.buildlogic

import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * Adds the test dependencies that are universal by project policy rather than
 * by module choice: JUnit 4 as the test runner and Konsist for the
 * architecture tests every module colocates with the code it verifies (see
 * `docs/modularization.md`).
 *
 * Every base convention plugin calls this, so a new module is born with the
 * architecture-test toolchain already on its test classpath — the policy is
 * impossible to forget. Test libraries that reflect what a module actually
 * exercises (MockK, Turbine, coroutines-test) intentionally stay declared per
 * module: hiding those would obscure each module's real testing surface.
 */
internal fun Project.configureUniversalTestDependencies() {
    dependencies {
        "testImplementation"(libs.library("junit"))
        "testImplementation"(libs.library("konsist"))
    }
}
