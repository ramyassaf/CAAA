package com.compose.chi.presentation.screens.my_favourite_jokes_page

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.compose.chi.ChiApplication
import com.compose.chi.data.database.JokeDao
import com.compose.chi.data.database.model.toJoke
import com.compose.chi.presentation.helpers.viewModelFactory
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

    private val _allLikedJokes = jokeDao.getAllLikedJokes()

    private val _state = MutableStateFlow(MyFavouriteJokesState())
    val state = combine(_state, _allLikedJokes) { state, allLikedJokes ->
        state.copy(
            jokes = allLikedJokes.map { it.toJoke() },
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

    // ViewModel Factory in a companion object
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            val jokeDao: JokeDao = ChiApplication.appModule.db.dao
            MyFavouriteJokesViewModel(jokeDao)
        }
    }
}