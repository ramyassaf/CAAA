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
        register("moduleArchitecture") {
            id = "com.compose.chi.module-architecture"
            implementationClass = "com.compose.chi.buildlogic.ModuleArchitecturePlugin"
        }
    }
}
