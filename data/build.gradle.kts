plugins {
    id("com.compose.chi.android.library")
    id("com.compose.chi.koin")
    alias(libs.plugins.devToolsKsp)
}

android {
    namespace = "com.compose.chi.data"

    testFixtures {
        enable = true
    }
}

dependencies {
    implementation(projects.domain)

    // Retrofit / OkHttp
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)

    // Room
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Test fixtures (data-specific Dto/Entity factories) reuse :domain Joke factories
    testFixturesImplementation(testFixtures(projects.domain))

    // Unit tests
    testImplementation(testFixtures(projects.domain))
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.okhttp)

    // Instrumented tests
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
