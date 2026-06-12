plugins {
    id("com.compose.chi.android.library")
    alias(libs.plugins.devToolsKsp)
}

android {
    namespace = "com.compose.chi.data"

    testFixtures {
        enable = true
    }
}

dependencies {
    implementation(project(":domain"))

    // Retrofit / OkHttp
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)

    // Room
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Koin — data-level providers live here
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.android)

    // Test fixtures (data-specific Dto/Entity factories) reuse :domain Joke factories
    testFixturesImplementation(testFixtures(project(":domain")))

    // Unit tests
    testImplementation(testFixtures(project(":domain")))
    testImplementation(libs.junit)
    testImplementation(libs.konsist)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.okhttp)

    // Instrumented tests
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
