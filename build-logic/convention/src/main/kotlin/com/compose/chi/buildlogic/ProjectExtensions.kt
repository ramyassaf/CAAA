package com.compose.chi.buildlogic

import org.gradle.api.Project
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.getByType

/**
 * The main build's version catalog (`gradle/libs.versions.toml`), as seen from
 * convention plugin code.
 *
 * Build scripts get the type-safe `libs` accessor generated for them; plugin
 * code reads the same catalog through [VersionCatalogsExtension] instead, so
 * versions and coordinates keep a single source of truth across both worlds.
 */
internal val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

/**
 * Returns the version declared under [alias] in `[versions]`, failing eagerly
 * with the alias name when it is missing — a misspelled alias should break the
 * build at configuration time, not produce a half-configured module.
 */
internal fun VersionCatalog.version(alias: String): String =
    findVersion(alias)
        .orElseThrow { IllegalStateException("Version '$alias' is missing from the version catalog.") }
        .requiredVersion

/**
 * Returns the library declared under [alias] in `[libraries]`, with the same
 * fail-eagerly contract as [version].
 */
internal fun VersionCatalog.library(alias: String): Provider<MinimalExternalModuleDependency> =
    findLibrary(alias)
        .orElseThrow { IllegalStateException("Library '$alias' is missing from the version catalog.") }
