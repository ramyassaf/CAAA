package com.compose.chi.architecture

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.interfaceDeclarations
import com.lemonappdev.konsist.api.ext.list.parentInterfaces
import com.lemonappdev.konsist.api.ext.list.sourceDeclarations
import com.lemonappdev.konsist.api.ext.list.withNameEndingWith
import com.lemonappdev.konsist.api.ext.list.withPath
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.Test

class RepositoryArchitectureTest {

    private val productionInterfaces = Konsist.scopeFromProject()
        .interfaces()
        .withPath("..src/main/java..")
    private val repositoryImplementations = Konsist.scopeFromProject()
        .classes()
        .withPath("..src/main/java..")
        .withNameEndingWith("RepositoryImpl")

    @Test
    fun `interfaces ending with Repository should reside in domain repository package`() {
        productionInterfaces.withNameEndingWith("Repository").assertTrue {
            it.resideInPackage("..domain.repository..")
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
