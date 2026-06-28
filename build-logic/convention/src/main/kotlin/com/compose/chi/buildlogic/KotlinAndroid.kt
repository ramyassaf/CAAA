package com.compose.chi.buildlogic

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project

/**
 * Configures the Android settings shared by every Android module, application
 * and library alike: the compile SDK, the minimum SDK, the instrumentation
 * runner, and the Java compatibility level.
 *
 * Both Android convention plugins funnel through this one function and read
 * the numbers from the version catalog, so the application and library
 * archetypes cannot drift apart and an SDK bump is a one-line catalog change.
 */
internal fun Project.configureKotlinAndroid(commonExtension: CommonExtension) {
    commonExtension.apply {
        compileSdk = libs.version("compileSdk").toInt()

        with(defaultConfig) {
            minSdk = libs.version("minSdk").toInt()
            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }

        with(compileOptions) {
            val javaVersion = JavaVersion.toVersion(libs.version("java"))
            sourceCompatibility = javaVersion
            targetCompatibility = javaVersion
        }
    }
}
