@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.compose.chi.presentation.screens.my_favourite_jokes_page

import app.cash.turbine.test
import com.compose.chi.domain.use_case.DeleteAllJokesUseCase
import com.compose.chi.domain.use_case.GetLikedJokesUseCase
import com.compose.chi.testing.FakeJokeRepository
import com.compose.chi.testing.MainDispatcherRule
import com.compose.chi.testing.TestJokes
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test

class MyFavouriteJokesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun viewModel(repo: FakeJokeRepository) = MyFavouriteJokesViewModel(
        getLikedJokesUseCase = GetLikedJokesUseCase(repo),
        deleteAllJokes = DeleteAllJokesUseCase(repo)
    )

    @Test
    fun `liked jokes flow updates state with emitted list`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val likedJokes = listOf(TestJokes.joke1Favourite, TestJokes.joke2Favourite)
            val repo = FakeJokeRepository().apply { setLikedJokes(likedJokes) }
            val vm = viewModel(repo)

            vm.state.test {
                // stateIn initial value is the default; once combine emits,
                // the next item carries the liked jokes from the fake.
                assertEquals(MyFavouriteJokesState(), awaitItem())
                assertEquals(
                    MyFavouriteJokesState(jokes = likedJokes, isLoading = false),
                    awaitItem()
                )
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `isLoading is false once liked jokes have been emitted`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val repo = FakeJokeRepository().apply {
                setLikedJokes(listOf(TestJokes.joke1Favourite))
            }
            val vm = viewModel(repo)

            // WhileSubscribed(5000) — must have an active subscriber for upstream to emit
            vm.state.test {
                skipItems(1) // default initial
                val emitted = awaitItem()
                assertFalse(emitted.isLoading)
                assertEquals(listOf(TestJokes.joke1Favourite), emitted.jokes)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `deleteAllItemsFromDb delegates to DeleteAllJokesUseCase`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val repo = FakeJokeRepository().apply {
                setLikedJokes(listOf(TestJokes.joke1Favourite))
            }
            val vm = viewModel(repo)
            advanceUntilIdle()

            vm.deleteAllItemsFromDb()
            advanceUntilIdle()

            assertEquals(1, repo.deleteAllCallCount)
        }

    @Test
    fun `state updates to empty list after deleteAll clears liked jokes`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val repo = FakeJokeRepository().apply {
                setLikedJokes(listOf(TestJokes.joke1Favourite, TestJokes.joke2Favourite))
            }
            val vm = viewModel(repo)

            vm.state.test {
                // skip default + initial liked emission
                assertEquals(MyFavouriteJokesState(), awaitItem())
                assertEquals(
                    MyFavouriteJokesState(
                        jokes = listOf(TestJokes.joke1Favourite, TestJokes.joke2Favourite),
                        isLoading = false
                    ),
                    awaitItem()
                )

                vm.deleteAllItemsFromDb()
                assertEquals(
                    MyFavouriteJokesState(jokes = emptyList(), isLoading = false),
                    awaitItem()
                )

                cancelAndIgnoreRemainingEvents()
            }
        }
}
