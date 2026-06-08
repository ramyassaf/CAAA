@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.compose.chi.presentation.screens.my_favourite_jokes_page

import app.cash.turbine.test
import com.compose.chi.domain.result.DomainError
import com.compose.chi.domain.result.Resource
import com.compose.chi.domain.use_case.DeleteAllJokesUseCase
import com.compose.chi.domain.use_case.ObserveLikedJokesUseCase
import com.compose.chi.testing.FakeJokeRepository
import com.compose.chi.testing.MainDispatcherRule
import com.compose.chi.testing.TestJokes
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class MyFavouriteJokesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun viewModel(repo: FakeJokeRepository) = MyFavouriteJokesViewModel(
        observeLikedJokesUseCase = ObserveLikedJokesUseCase(repo),
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
                    MyFavouriteJokesState(jokes = likedJokes),
                    awaitItem()
                )
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
    fun `liked jokes persistence error updates state with non-blank error`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val repo = FakeJokeRepository().apply {
                likedJokesError = DomainError.Persistence
            }
            val vm = viewModel(repo)

            vm.state.test {
                assertEquals(MyFavouriteJokesState(), awaitItem())
                val errorState = awaitItem()
                assertNotNull(errorState.error)
                assertTrue("error must be non-blank", errorState.error.isNotBlank())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `deleteAll persistence error updates state with non-blank error`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val repo = FakeJokeRepository().apply {
                deleteAllResource = Resource.Error(DomainError.Persistence)
            }
            val vm = viewModel(repo)

            vm.state.test {
                assertEquals(MyFavouriteJokesState(), awaitItem())

                vm.deleteAllItemsFromDb()
                val errorState = awaitItem()

                assertNotNull(errorState.error)
                assertTrue("error must be non-blank", errorState.error.isNotBlank())
                cancelAndIgnoreRemainingEvents()
            }
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
                        jokes = listOf(TestJokes.joke1Favourite, TestJokes.joke2Favourite)
                    ),
                    awaitItem()
                )

                vm.deleteAllItemsFromDb()
                assertEquals(
                    MyFavouriteJokesState(jokes = emptyList()),
                    awaitItem()
                )

                cancelAndIgnoreRemainingEvents()
            }
        }
}
