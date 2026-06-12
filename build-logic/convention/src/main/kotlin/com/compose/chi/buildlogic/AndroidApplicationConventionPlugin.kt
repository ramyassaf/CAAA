package com.compose.chi.buildlogic

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/**
 * Convention plugin for the Android application module
 * (`com.compose.chi.android.application`).
 *
 * Applies the Android application plugin and layers the project-wide Android
 * conventions from [configureKotlinAndroid] on top, plus the settings only an
 * application has: the target SDK and the packaging rules that strip
 * duplicated license files pulled in by coroutines artifacts.
 *
 * What it deliberately does not own: the application id, versioning, build
 * types, and module dependencies. Those describe *this* app rather than "how
 * an application module is built" and stay in `app/build.gradle.kts`.
 */
class AndroidApplicationConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.android.application")

            extensions.configure<ApplicationExtension> {
                configureKotlinAndroid(this)

                defaultConfig.targetSdk = libs.version("targetSdk").toInt()

                packaging {
                    resources {
                        excludes += "/META-INF/{AL2.0,LGPL2.1}"
                    }
                }
            }
        }
    }
}
