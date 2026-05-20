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
}
