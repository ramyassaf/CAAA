package com.compose.chi.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.kotlin.dsl.register

/**
 * Convention plugin that enforces the project's Clean Architecture module dependency
 * direction at build time.
 *
 * Applied to the root project via `id("com.compose.chi.module-architecture")`, it registers
 * the `verifyModuleArchitecture` task, which fails the build whenever the module dependency
 * graph drifts from the intended architecture. Every module may depend only on the modules
 * listed in [architectureContract]: `:domain` stays dependency-free, `:data` and
 * `:presentation` may depend only on `:domain`, and `:app` (the composition root) may depend
 * only on `:presentation` and `:data`.
 *
 * The plugin captures the actual graph through lazy providers that Gradle evaluates once
 * every project has been configured, and hands the result to [VerifyModuleArchitectureTask]
 * as plain value inputs. The task action never touches `Project` state at execution time,
 * keeping the check compatible with the configuration cache; any build-script change
 * invalidates that cache, so a reused entry always reflects the current declarations.
 *
 * The snapshot reads declared dependencies through Gradle's project and configuration APIs
 * rather than parsing build scripts, so it stays accurate as the build grows. Only
 * production configurations are inspected (see [isProductionConfiguration]); test
 * configurations are ignored so that shared test fixtures remain free to cross module
 * boundaries.
 *
 * Scope: the task verifies the *module dependency graph* only — a complete, structural
 * rule. Other dependency concerns are left to module boundaries, `implementation`/`api`
 * scoping, and code review.
 */
class ModuleArchitecturePlugin : Plugin<Project> {

    /**
     * Wires the architecture verification into the target (root) project.
     *
     * Applies the `base` plugin so the standard `check` lifecycle task exists, registers
     * `verifyModuleArchitecture` under the `verification` group, and attaches it to `check`
     * so the rule runs as part of the standard verification lifecycle — locally via
     * `./gradlew check` and in CI.
     */
    override fun apply(target: Project) {
        // The root project does not apply the Android or Kotlin plugins, so it has no
        // `check` lifecycle task of its own. `base` provides it, giving the verification a
        // standard task to hook onto.
        target.pluginManager.apply("base")

        val verifyModuleArchitecture = target.tasks.register<VerifyModuleArchitectureTask>(
            "verifyModuleArchitecture"
        ) {
            group = "verification"
            description = "Verifies module-level architecture dependency boundaries."

            allowedProjectDependencies.set(architectureContract)

            // The providers stay unevaluated until Gradle fingerprints the task inputs,
            // which happens after every module has been configured — so the snapshot sees
            // each module's final declared dependencies.
            actualModulePaths.set(target.provider {
                target.subprojects.map { project -> project.path }.toSet()
            })
            actualProjectDependencies.set(target.provider {
                target.subprojects.associate { project ->
                    project.path to project.productionProjectDependencyPaths()
                }
            })
        }

        target.tasks.named("check") {
            dependsOn(verifyModuleArchitecture)
        }
    }

    /**
     * Returns the paths of the modules this project depends on across all production
     * configurations (for example `[":domain"]`). Only project-to-project
     * ([ProjectDependency]) dependencies are collected; external libraries are ignored.
     */
    private fun Project.productionProjectDependencyPaths(): Set<String> =
        productionConfigurations()
            .flatMap { configuration ->
                configuration.dependencies
                    .withType(ProjectDependency::class.java)
                    .map { dependency -> dependency.path }
            }
            .toSet()

    /**
     * The production configurations on this project — every configuration that is not part
     * of a test source set (see [isProductionConfiguration]).
     *
     * Deriving the set dynamically, rather than hard-coding configuration names, means new
     * modules, build variants, and plugins are covered automatically as the project grows.
     */
    private fun Project.productionConfigurations(): List<Configuration> =
        configurations.filter { configuration -> configuration.isProductionConfiguration() }

    private companion object {
        /**
         * The allowed module dependency graph — the single source of truth for the
         * architecture contract.
         *
         * Keys are every module expected in the build; the actual set of modules is checked
         * against these keys. Each value is the exact set of project dependencies the module
         * is permitted to declare in production configurations.
         */
        val architectureContract: Map<String, Set<String>> = mapOf(
            ":domain" to emptySet(),
            ":data" to setOf(":domain"),
            ":presentation" to setOf(":domain"),
            ":app" to setOf(":presentation", ":data"),
        )

        /**
         * Whether dependencies declared on this configuration count as production code.
         *
         * A configuration is treated as test-only when its name starts with `test` (unit
         * test and test-fixture configurations such as `testImplementation` and
         * `testFixturesApi`) or contains `Test` (for example `androidTestImplementation` or
         * `kspTest`); everything else is production. Matching on the name keeps the rule
         * independent of the exact plugins applied and resilient as build variants are added,
         * while the `startsWith`/`contains "Test"` split avoids misclassifying unrelated
         * names such as a `latest` product flavor.
         */
        fun Configuration.isProductionConfiguration(): Boolean =
            !(name.startsWith("test") || name.contains("Test"))
    }
}
