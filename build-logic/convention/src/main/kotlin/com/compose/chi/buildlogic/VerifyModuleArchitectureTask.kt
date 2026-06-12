package com.compose.chi.buildlogic

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

/**
 * Compares the module dependency graph that was captured at configuration time
 * against the architecture contract, and fails the build listing every
 * violation at once.
 *
 * The task consumes plain, serializable values only — the actual graph arrives
 * through [Input] properties wired by [ModuleArchitecturePlugin]. The action
 * never reaches back into `Project` state at execution time, which is exactly
 * what the configuration cache forbids; keeping the inputs pure values is what
 * makes the verification cache-compatible. As a bonus, the values participate
 * in up-to-date checking: the task only re-runs when the graph or the contract
 * actually changes.
 */
abstract class VerifyModuleArchitectureTask : DefaultTask() {

    /**
     * The architecture contract: every module expected in the build, mapped to
     * the exact set of project dependencies it may declare in production
     * configurations.
     */
    @get:Input
    abstract val allowedProjectDependencies: MapProperty<String, Set<String>>

    /** The module paths actually declared in the build. */
    @get:Input
    abstract val actualModulePaths: SetProperty<String>

    /**
     * Each declared module's actual production project dependencies, as
     * snapshotted after every project finished configuring.
     */
    @get:Input
    abstract val actualProjectDependencies: MapProperty<String, Set<String>>

    /**
     * Validates the captured graph and, if anything is off, throws a
     * [GradleException] listing every violation.
     *
     * All violations are collected before failing (rather than failing on the
     * first one) so a single run reports every boundary problem at once. Two
     * checks are performed: the set of declared modules, and each module's
     * production project dependencies.
     */
    @TaskAction
    fun verify() {
        val contract: Map<String, Set<String>> = allowedProjectDependencies.get()
        val modulePaths: Set<String> = actualModulePaths.get()
        val dependenciesByModule: Map<String, Set<String>> = actualProjectDependencies.get()
        val violations: MutableList<String> = mutableListOf()

        // 1. The set of modules in the build must match the contract exactly, so a new or
        //    removed module cannot silently escape verification.
        val expectedModulePaths: Set<String> = contract.keys
        if (modulePaths != expectedModulePaths) {
            violations.add(buildString {
                append("Declared modules differ from the architecture contract. ")
                append("Expected ${expectedModulePaths.sorted()}, actual ${modulePaths.sorted()}.")
            })
        }

        // 2. Each module's production project dependencies must match its allowed set
        //    exactly — no unexpected dependencies and no missing ones. A contract module
        //    absent from the build reports as empty here; check 1 already flags it.
        contract.forEach { (modulePath, expectedDependencies) ->
            val actualDependencies: Set<String> = dependenciesByModule[modulePath].orEmpty()

            if (actualDependencies != expectedDependencies) {
                violations.add(buildString {
                    append("$modulePath has invalid production project dependencies. ")
                    append("Expected ${expectedDependencies.sorted()}, actual ${actualDependencies.sorted()}.")

                    val unexpectedDependencies: Set<String> = actualDependencies - expectedDependencies
                    if (unexpectedDependencies.isNotEmpty()) {
                        append(" Unexpected ${unexpectedDependencies.sorted()}.")
                    }

                    val missingDependencies: Set<String> = expectedDependencies - actualDependencies
                    if (missingDependencies.isNotEmpty()) {
                        append(" Missing ${missingDependencies.sorted()}.")
                    }
                })
            }
        }

        if (violations.isNotEmpty()) {
            throw GradleException(
                "Module architecture verification failed:\n" +
                    violations.joinToString(separator = "\n") { violation -> "- $violation" }
            )
        }
    }
}
