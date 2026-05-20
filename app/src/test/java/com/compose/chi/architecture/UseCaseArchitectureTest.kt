package com.compose.chi.architecture

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.imports
import com.lemonappdev.konsist.api.ext.list.withNameEndingWith
import com.lemonappdev.konsist.api.ext.list.withPath
import com.lemonappdev.konsist.api.verify.assertFalse
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.Test

class UseCaseArchitectureTest {

    private val productionFiles = Konsist.scopeFromProject().files.withPath("..src/main/java..")
    private val useCaseClasses = Konsist.scopeFromProject()
        .classes()
        .withPath("..src/main/java..")
        .withNameEndingWith("UseCase")
    private val useCaseFiles = productionFiles.withPath("..domain/use_case..")

    private val prohibitedUseCaseImports = setOf(
        "android.",
        "androidx.",
        "androidx.room",
        "com.compose.chi.data.",
        "java.io",
        "java.net",
        "okhttp3",
        "org.koin",
        "retrofit2",
    )

    @Test
    fun `classes ending with UseCase should reside in domain use case package`() {
        useCaseClasses.assertTrue { it.resideInPackage("..domain.use_case..") }
    }

    @Test
    fun `classes ending with UseCase should expose operator invoke`() {
        useCaseClasses.assertTrue { useCase ->
            useCase.hasFunction { function ->
                function.name == "invoke" && function.hasOperatorModifier
            }
        }
    }

    @Test
    fun `classes ending with UseCase should have exactly one public function`() {
        useCaseClasses.assertTrue { useCase ->
            useCase.countFunctions { function -> function.hasPublicOrDefaultModifier } == 1
        }
    }

    @Test
    fun `use cases must not import infrastructure packages`() {
        useCaseFiles.imports.assertFalse { import ->
            prohibitedUseCaseImports.any { prohibited -> import.name.startsWith(prohibited) }
        }
    }

    @Test
    fun `use cases should depend on repository contracts not implementations`() {
        useCaseFiles.imports.assertFalse { import ->
            import.name.endsWith("RepositoryImpl")
        }
    }
}
