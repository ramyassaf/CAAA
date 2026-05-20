package com.compose.chi.architecture

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.verify.assertFalse
import org.junit.Test

class ProjectArchitectureTest {

    @Test
    fun `project Kotlin files should not use wildcard imports`() {
        Konsist.scopeFromProject().imports.assertFalse { import -> import.isWildcard }
    }

    @Test
    fun `project Kotlin files should not be empty`() {
        Konsist.scopeFromProject().files.assertFalse { file -> file.text.isBlank() }
    }

    @Test
    fun `production files should not contain TODO architecture comments`() {
        Konsist.scopeFromProject().files.assertFalse { file ->
            file.path.contains("src/main/java") &&
                file.text.contains("TODO", ignoreCase = true)
        }
    }
}
