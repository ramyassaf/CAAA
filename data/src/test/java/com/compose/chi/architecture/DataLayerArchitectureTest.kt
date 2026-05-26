package com.compose.chi.architecture

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.imports
import com.lemonappdev.konsist.api.ext.list.interfaceDeclarations
import com.lemonappdev.konsist.api.ext.list.parentInterfaces
import com.lemonappdev.konsist.api.ext.list.properties
import com.lemonappdev.konsist.api.ext.list.sourceDeclarations
import com.lemonappdev.konsist.api.ext.list.withNameEndingWith
import com.lemonappdev.konsist.api.ext.list.withPackage
import com.lemonappdev.konsist.api.ext.list.withPath
import com.lemonappdev.konsist.api.verify.assertFalse
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.Test

class DataLayerArchitectureTest {

    private val productionFiles = Konsist.scopeFromProject().files.withPath("..src/main/java..")
    private val dataFiles = productionFiles.withPath("..data..")
    private val dtoClasses = Konsist.scopeFromProject()
        .classes()
        .withPath("..src/main/java..")
        .withNameEndingWith("Dto")
    private val entityClasses = Konsist.scopeFromProject()
        .classes()
        .withPath("..src/main/java..")
        .withNameEndingWith("Entity")
    private val apiInterfaces = Konsist.scopeFromProject()
        .interfaces()
        .withPath("..src/main/java..")
        .withNameEndingWith("Api")
    private val repositoryImplementations = Konsist.scopeFromProject()
        .classes()
        .withPath("..src/main/java..")
        .withNameEndingWith("RepositoryImpl")

    @Test
    fun `data layer must not depend on presentation`() {
        dataFiles.imports.assertFalse { import ->
            import.name.startsWith("com.compose.chi.presentation.")
        }
    }

    @Test
    fun `DTO classes should reside in data remote dto package`() {
        dtoClasses.assertTrue { it.resideInPackage("..data.remote.dto..") }
    }

    @Test
    fun `DTO classes should be data classes with immutable properties`() {
        dtoClasses.assertTrue { it.hasDataModifier }
        dtoClasses.properties(includeNested = false).assertTrue { property -> property.isVal }
    }

    @Test
    fun `Entity classes should reside in data database model package`() {
        entityClasses.assertTrue { it.resideInPackage("..data.database.model..") }
    }

    @Test
    fun `Entity classes should be data classes with immutable properties`() {
        entityClasses.assertTrue { it.hasDataModifier }
        entityClasses.properties(includeNested = false).assertTrue { property -> property.isVal }
    }

    @Test
    fun `technical exception mapping should remain in data repository`() {
        dataFiles.withPackage("..data.repository..").assertTrue { file ->
            file.text.contains("HttpException") ||
                file.text.contains("IOException") ||
                file.text.contains("DomainError.Persistence")
        }
    }

    @Test
    fun `interfaces ending with Api should reside in data remote package`() {
        apiInterfaces.assertTrue { it.resideInPackage("..data.remote..") }
    }

    @Test
    fun `Retrofit annotations should be imported only from data remote package`() {
        productionFiles.assertTrue { file ->
            !file.imports.any { import -> import.name.startsWith("retrofit2.http.") } ||
                file.text.startsWith("package com.compose.chi.data.remote")
        }
    }

    @Test
    fun `classes ending with RepositoryImpl should reside in data repository package`() {
        repositoryImplementations.assertTrue { it.resideInPackage("..data.repository..") }
    }

    @Test
    fun `repository implementations should implement a domain repository interface`() {
        repositoryImplementations
            .parentInterfaces()
            .sourceDeclarations()
            .interfaceDeclarations()
            .assertTrue { it.resideInPackage("..domain.repository..") }
    }
}
