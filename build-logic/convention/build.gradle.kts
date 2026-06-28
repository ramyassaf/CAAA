plugins {
    `kotlin-dsl`
}

group = "com.compose.chi.buildlogic"

dependencies {
    // compileOnly: the convention plugins need AGP's DSL types (Application/Library/
    // CommonExtension — used by the Android plugins) and the Kotlin JVM DSL
    // (KotlinJvmProjectExtension — used only by the kotlin.jvm plugin for :domain)
    // to COMPILE. The root `plugins { ... apply false }` block already puts these
    // same artifacts on the build classpath at runtime, so we compile against them
    // without contributing a second copy.
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "com.compose.chi.android.application"
            implementationClass = "com.compose.chi.buildlogic.AndroidApplicationConventionPlugin"
        }
        register("androidCompose") {
            id = "com.compose.chi.android.compose"
            implementationClass = "com.compose.chi.buildlogic.AndroidComposeConventionPlugin"
        }
        register("androidLibrary") {
            id = "com.compose.chi.android.library"
            implementationClass = "com.compose.chi.buildlogic.AndroidLibraryConventionPlugin"
        }
        register("koin") {
            id = "com.compose.chi.koin"
            implementationClass = "com.compose.chi.buildlogic.KoinConventionPlugin"
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
