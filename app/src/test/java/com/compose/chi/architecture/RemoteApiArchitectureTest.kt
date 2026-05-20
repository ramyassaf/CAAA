package com.compose.chi.architecture

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.imports
import com.lemonappdev.konsist.api.ext.list.withNameEndingWith
import com.lemonappdev.konsist.api.ext.list.withPath
import com.lemonappdev.konsist.api.verify.assertFalse
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.Test

class RemoteApiArchitectureTest {

    private val productionFiles = Konsist.scopeFromProject().files.withPath("..src/main/java..")
    private val apiInterfaces = Konsist.scopeFromProject()
        .interfaces()
        .withPath("..src/main/java..")
        .withNameEndingWith("Api")

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
    fun `domain must not contain classes or interfaces ending with Api`() {
        apiInterfaces.assertFalse { it.resideInPackage("..domain..") }
    }

    @Test
    fun `presentation must not import Retrofit Api classes directly`() {
        productionFiles.withPath("..presentation..").imports.assertFalse { import ->
            import.name.startsWith("com.compose.chi.data.remote.") ||
                import.name.contains("retrofit2")
        }
    }
}
