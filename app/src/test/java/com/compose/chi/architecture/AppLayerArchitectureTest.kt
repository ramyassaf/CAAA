package com.compose.chi.architecture

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.imports
import com.lemonappdev.konsist.api.ext.list.withPath
import com.lemonappdev.konsist.api.ext.list.withoutPath
import com.lemonappdev.konsist.api.verify.assertFalse
import org.junit.Test

class AppLayerArchitectureTest {

    private val appProductionFiles = Konsist.scopeFromProject()
        .files
        .withPath("..app/src/main/java/com/compose/chi..")

    private val appOutsideCompositionRoot = appProductionFiles
        .withoutPath("..app/src/main/java/com/compose/chi/ChiApplication.kt")
        .withoutPath("..app/src/main/java/com/compose/chi/di..")

    /**
     * The `:data` module is an implementation detail. Only the composition
     * root (`ChiApplication`) and the app-level DI module under
     * `com.compose.chi.di` are allowed to reference it directly so they can
     * wire concrete implementations into Koin.
     *
     * Everything else — presentation screens, ViewModels, navigation, theme,
     * analytics — must depend on `:domain` abstractions instead.
     */
    @Test
    fun `only ChiApplication and app DI composition may import the data layer`() {
        appOutsideCompositionRoot.imports.assertFalse { import ->
            import.name.startsWith("com.compose.chi.data.")
        }
    }

    /**
     * Retrofit is a transport-layer concern owned by `:data`. The Gradle
     * module split already removes retrofit2 from `:app`'s compile classpath;
     * this guard documents the rule at source level and catches accidental
     * reintroduction in code review before it ever reaches the build.
     */
    @Test
    fun `app layer must not reference Retrofit types`() {
        appProductionFiles.imports.assertFalse { import ->
            import.name.contains("retrofit2")
        }
    }
}
