@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.compose.chi.presentation.screens.joke_home_page

import app.cash.turbine.test
import com.compose.chi.domain.use_case.GetJokeUseCase
import com.compose.chi.domain.use_case.ObserveJokeLikedStatusUseCase
import com.compose.chi.domain.use_case.UpsertJokeUseCase
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

class JokeHomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun viewModel(repo: FakeJokeRepository) = JokeHomeViewModel(
        getJokeUseCase = GetJokeUseCase(repo),
        observeJokeLikedStatusUseCase = ObserveJokeLikedStatusUseCase(repo),
        upsertJokeUseCase = UpsertJokeUseCase(repo)
    )

    @Test
    fun `initial load emits loading then success state from getJoke`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val repo = FakeJokeRepository().apply { jokeResult = TestJokes.joke1 }
            val vm = viewModel(repo)

            vm.state.test {
                assertEquals(JokeHomeState(), awaitItem())
                assertEquals(JokeHomeState(isLoading = true), awaitItem())
                assertEquals(JokeHomeState(joke = TestJokes.joke1), awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `success state reflects liked status from ObserveJokeLikedStatusUseCase`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val repo = FakeJokeRepository().apply {
                jokeResult = TestJokes.joke1
                setLikedStatus(TestJokes.joke1.id, true)
            }
            val vm = viewModel(repo)

            vm.state.test {
                // default, loading, then success — already marked favourite
                assertEquals(JokeHomeState(), awaitItem())
                assertEquals(JokeHomeState(isLoading = true), awaitItem())
                assertEquals(JokeHomeState(joke = TestJokes.joke1Favourite), awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `liked status flow update changes state joke isFavourite`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val repo = FakeJokeRepository().apply { jokeResult = TestJokes.joke1 }
            val vm = viewModel(repo)

            vm.state.test {
                // skip default + loading + initial success (not liked)
                assertEquals(JokeHomeState(), awaitItem())
                assertEquals(JokeHomeState(isLoading = true), awaitItem())
                assertEquals(JokeHomeState(joke = TestJokes.joke1), awaitItem())

                repo.setLikedStatus(TestJokes.joke1.id, true)
                assertEquals(JokeHomeState(joke = TestJokes.joke1Favourite), awaitItem())

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `toggleLikeJokeInDb updates state and delegates to UpsertJokeUseCase`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val repo = FakeJokeRepository().apply { jokeResult = TestJokes.joke1 }
            val vm = viewModel(repo)
            advanceUntilIdle()

            vm.toggleLikeJokeInDb(TestJokes.joke1)
            advanceUntilIdle()

            assertEquals(TestJokes.joke1Favourite, vm.state.value.joke)
            assertEquals(listOf(TestJokes.joke1Favourite), repo.upsertedJokes)
        }

    @Test
    fun `error from GetJokeUseCase produces state with non-blank error`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val repo = FakeJokeRepository().apply {
                jokeError = HttpException(
                    Response.error<Any>(
                        500,
                        "boom".toResponseBody("text/plain".toMediaType())
                    )
                )
            }
            val vm = viewModel(repo)
            advanceUntilIdle()

            val state = vm.state.value
            assertNotNull(state.error)
            assertTrue("error must be non-blank", state.error.isNotBlank())
        }
}
