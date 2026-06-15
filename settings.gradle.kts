pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
// Generate type-safe accessors (projects.domain) for project dependencies,
// so a renamed or mistyped module path fails the build instead of compiling
// against a silently wrong string.
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "CHI"
include(":app")
include(":presentation")
include(":domain")
include(":data")
