plugins {
    `kotlin-dsl`
}

group = "com.compose.chi.buildlogic"

gradlePlugin {
    plugins {
        register("moduleArchitecture") {
            id = "com.compose.chi.module-architecture"
            implementationClass = "com.compose.chi.buildlogic.ModuleArchitecturePlugin"
        }
    }
}
