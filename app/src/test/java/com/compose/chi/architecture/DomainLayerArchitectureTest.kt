package com.compose.chi.architecture

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.imports
import com.lemonappdev.konsist.api.ext.list.properties
import com.lemonappdev.konsist.api.ext.list.withNameEndingWith
import com.lemonappdev.konsist.api.ext.list.withPackage
import com.lemonappdev.konsist.api.ext.list.withPath
import com.lemonappdev.konsist.api.verify.assertFalse
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.Test

class DomainLayerArchitectureTest {

    private val productionFiles = Konsist.scopeFromProject().files.withPath("..src/main/java..")
    private val domainFiles = productionFiles.withPath("..domain..")
    private val domainModels = Konsist.scopeFromProject()
        .classes()
        .withPath("..src/main/java..")
        .withPackage("..domain.model..")
    private val domainRepositories = Konsist.scopeFromProject()
        .interfaces()
        .withPath("..src/main/java..")
        .withPackage("..domain.repository..")

    private val prohibitedDomainImports = setOf(
        "android.",
        "androidx.",
        "androidx.room",
        "com.compose.chi.data.",
        "com.compose.chi.presentation.",
        "java.io",
        "java.net",
        "okhttp3",
        "org.koin",
        "retrofit2",
    )

    @Test
    fun `domain production files must not import data presentation platform or infrastructure packages`() {
        domainFiles.imports.assertFalse { import ->
            prohibitedDomainImports.any { prohibited -> import.name.startsWith(prohibited) }
        }
    }

    @Test
    fun `domain production files must not import data implementation concepts`() {
        domainFiles.imports.assertFalse { import ->
            listOf("Dto", "Entity", "Dao", "Api", "Database", "RepositoryImpl").any {
                import.name.contains(it)
            }
        }
    }

    @Test
    fun `domain models should use immutable properties`() {
        domainModels.properties(includeNested = false).assertTrue { property -> property.isVal }
    }

    @Test
    fun `domain repository interfaces should reside in domain repository package`() {
        domainRepositories.withNameEndingWith("Repository").assertTrue {
            it.resideInPackage("..domain.repository..")
        }
    }

    @Test
    fun `domain result abstractions should reside in domain result package`() {
        Konsist.scopeFromProject()
            .interfaces()
            .withPath("..src/main/java..")
            .withNameEndingWith("Resource")
            .assertTrue { it.resideInPackage("..domain.result..") }

        Konsist.scopeFromProject()
            .interfaces()
            .withPath("..src/main/java..")
            .withNameEndingWith("Error")
            .assertTrue { it.resideInPackage("..domain.result..") }
    }
}
