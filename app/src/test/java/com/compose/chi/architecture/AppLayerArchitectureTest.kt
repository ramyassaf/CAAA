package com.compose.chi.architecture

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.imports
import com.lemonappdev.konsist.api.ext.list.withPath
import com.lemonappdev.konsist.api.ext.list.withoutPath
import com.lemonappdev.konsist.api.verify.assertFalse
import org.junit.Test

class AppLayerArchitectureTest {

    private val allowedDataKoinModuleImport = "com.compose.chi.data.di.dataKoinModule"
    private val appProductionFiles = Konsist.scopeFromProject()
        .files
        .withPath("..app/src/main/java/com/compose/chi..")

    /**
     * `:app` depends on `:data` only because the Android application class is
     * the composition root. Keep that exception narrow: `ChiApplication` may
     * install the data KOIN module, but no app-layer code should import data
     * implementation types directly.
     */
    @Test
    fun `only ChiApplication may import the data layer`() {
        appProductionFiles
            .withoutPath("..app/src/main/java/com/compose/chi/ChiApplication.kt")
            .imports
            .assertFalse { import -> import.name.startsWith("com.compose.chi.data.") }
    }

    @Test
    fun `ChiApplication may import only the data Koin module from data`() {
        appProductionFiles
            .withPath("..app/src/main/java/com/compose/chi/ChiApplication.kt")
            .imports
            .assertFalse { import ->
                import.name.startsWith("com.compose.chi.data.") &&
                    import.name != allowedDataKoinModuleImport
            }
    }
}
