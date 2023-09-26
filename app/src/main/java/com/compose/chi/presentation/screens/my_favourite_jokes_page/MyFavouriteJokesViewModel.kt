package com.compose.chi.presentation.screens.my_favourite_jokes_page

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.compose.chi.data.database.JokeDao
import com.compose.chi.data.database.model.toJoke
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MyFavouriteJokesViewModel(
    private val jokeDao: JokeDao
): ViewModel() {

    private val _allJokes = jokeDao.getAllJokes()

    private val _state = MutableStateFlow(MyFavouriteJokesState())
    val state = combine(_state, _allJokes) { state, allJokes ->
        state.copy(
            jokes = allJokes.map { it.toJoke() },
            isLoading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MyFavouriteJokesState())

    fun deleteAllItemsFromDb() {
        // create a scope to access the database from a thread other than the main thread
        val scope = CoroutineScope(Dispatchers.Default)
        scope.launch {
            jokeDao.deleteAllJokes()
        }
    }
}