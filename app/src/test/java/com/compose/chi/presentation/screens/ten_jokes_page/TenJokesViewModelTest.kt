@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.compose.chi.presentation.screens.ten_jokes_page

import app.cash.turbine.test
import com.compose.chi.domain.use_case.GetTenJokesUseCase
import com.compose.chi.testing.FakeJokeRepository
import com.compose.chi.testing.MainDispatcherRule
import com.compose.chi.testing.TestJokes
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class TenJokesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun viewModel(repo: FakeJokeRepository) = TenJokesViewModel(
        getTenJokesUseCase = GetTenJokesUseCase(repo)
    )

    @Test
    fun `initial load emits loading then success list`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val jokes = TestJokes.tenJokes()
            val repo = FakeJokeRepository().apply { tenJokesResult = jokes }
            val vm = viewModel(repo)

            vm.state.test {
                assertEquals(TenJokesState(), awaitItem())
                assertEquals(TenJokesState(isLoading = true), awaitItem())
                assertEquals(TenJokesState(jokes = jokes), awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `error from GetTenJokesUseCase produces state with non-blank error`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val repo = FakeJokeRepository().apply {
                tenJokesError = HttpException(
                    Response.error<Any>(
                        503,
                        "down".toResponseBody("text/plain".toMediaType())
                    )
                )
            }
            val vm = viewModel(repo)
            advanceUntilIdle()

            val state = vm.state.value
            assertNotNull(state.error)
            assertTrue("error must be non-blank", state.error.isNotBlank())
        }

    @Test
    fun `calling getTenJokes again prepends new jokes to existing list`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val firstBatch = TestJokes.tenJokes()
            val secondBatch = (101..110).map { id ->
                com.compose.chi.domain.model.Joke(
                    id = id,
                    setup = "Second setup $id",
                    punchline = "Second punchline $id",
                    type = "general"
                )
            }
            val repo = FakeJokeRepository().apply { tenJokesResult = firstBatch }
            val vm = viewModel(repo)
            advanceUntilIdle()

            // sanity: first batch loaded
            assertEquals(firstBatch, vm.state.value.jokes)

            repo.tenJokesResult = secondBatch
            vm.getTenJokes()
            advanceUntilIdle()

            // current behavior: new batch is prepended
            assertEquals(secondBatch + firstBatch, vm.state.value.jokes)
        }
}
