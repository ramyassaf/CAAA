@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.compose.chi.presentation.screens.joke_details_page

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.compose.chi.domain.result.DomainError
import com.compose.chi.domain.result.Resource
import com.compose.chi.domain.use_case.GetJokeByIdUseCase
import com.compose.chi.domain.use_case.ObserveJokeLikedStatusUseCase
import com.compose.chi.domain.use_case.UpsertJokeUseCase
import com.compose.chi.testing.FakeJokeRepository
import com.compose.chi.testing.MainDispatcherRule
import com.compose.chi.testing.TestJokes
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class JokeDetailsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun viewModel(
        repo: FakeJokeRepository,
        savedStateHandle: SavedStateHandle = SavedStateHandle(mapOf("jokeId" to TestJokes.joke1.id.toString()))
    ) = JokeDetailsViewModel(
        savedStateHandle = savedStateHandle,
        getJokeByIdUseCase = GetJokeByIdUseCase(repo),
        observeJokeLikedStatusUseCase = ObserveJokeLikedStatusUseCase(repo),
        upsertJokeUseCase = UpsertJokeUseCase(repo)
    )

    @Test
    fun `initial load emits loading then success when jokeId is in SavedStateHandle`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val repo = FakeJokeRepository().apply {
                jokeByIdResource = Resource.Success(TestJokes.joke1)
            }
            val vm = viewModel(repo)

            vm.state.test {
                assertEquals(JokeDetailsState(), awaitItem())
                assertEquals(JokeDetailsState(isLoading = true), awaitItem())
                assertEquals(JokeDetailsState(joke = TestJokes.joke1), awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `use case receives the expected joke id from SavedStateHandle`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val jokeId = "99"
            val repo = FakeJokeRepository().apply {
                jokeByIdResource = Resource.Success(TestJokes.joke1)
            }
            viewModel(repo, SavedStateHandle(mapOf("jokeId" to jokeId)))
            advanceUntilIdle()

            assertEquals(jokeId, repo.lastRequestedJokeId)
        }

    @Test
    fun `liked status flow update changes state joke isFavourite`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val repo = FakeJokeRepository().apply {
                jokeByIdResource = Resource.Success(TestJokes.joke1)
            }
            val vm = viewModel(repo)

            vm.state.test {
                assertEquals(JokeDetailsState(), awaitItem())
                assertEquals(JokeDetailsState(isLoading = true), awaitItem())
                assertEquals(JokeDetailsState(joke = TestJokes.joke1), awaitItem())

                repo.setLikedStatus(TestJokes.joke1.id, true)
                assertEquals(JokeDetailsState(joke = TestJokes.joke1Favourite), awaitItem())

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `toggleLikeJokeInDb updates state and delegates to UpsertJokeUseCase`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val repo = FakeJokeRepository().apply {
                jokeByIdResource = Resource.Success(TestJokes.joke1)
            }
            val vm = viewModel(repo)
            advanceUntilIdle()

            vm.toggleLikeJokeInDb(TestJokes.joke1)
            advanceUntilIdle()

            assertEquals(TestJokes.joke1Favourite, vm.state.value.joke)
            assertEquals(listOf(TestJokes.joke1Favourite), repo.upsertedJokes)
        }

    @Test
    fun `error from GetJokeByIdUseCase produces state with non-blank error`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val repo = FakeJokeRepository().apply {
                jokeByIdResource = Resource.Error(DomainError.NotFound)
            }
            val vm = viewModel(repo)
            advanceUntilIdle()

            val state = vm.state.value
            assertNotNull(state.error)
            assertTrue("error must be non-blank", state.error.isNotBlank())
        }

    @Test
    fun `missing jokeId leaves state at default and does not call remote fetch`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val repo = FakeJokeRepository().apply {
                jokeByIdResource = Resource.Success(TestJokes.joke1)
            }
            val vm = viewModel(repo, SavedStateHandle(emptyMap()))
            advanceUntilIdle()

            assertEquals(JokeDetailsState(), vm.state.value)
            assertNull(repo.lastRequestedJokeId)
        }
}
