plugins {
    `kotlin-dsl`
}

group = "com.compose.chi.buildlogic"

dependencies {
    // compileOnly: convention plugins compile against the AGP and Kotlin
    // Gradle APIs, while the main build provides them at runtime through the
    // root plugins block (`apply false`), keeping a single copy on the build
    // classpath.
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "com.compose.chi.android.application"
            implementationClass = "com.compose.chi.buildlogic.AndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = "com.compose.chi.android.library"
            implementationClass = "com.compose.chi.buildlogic.AndroidLibraryConventionPlugin"
        }
        register("kotlinJvm") {
            id = "com.compose.chi.kotlin.jvm"
            implementationClass = "com.compose.chi.buildlogic.KotlinJvmConventionPlugin"
        }
        register("moduleArchitecture") {
            id = "com.compose.chi.module-architecture"
            implementationClass = "com.compose.chi.buildlogic.ModuleArchitecturePlugin"
        }
    }
}
