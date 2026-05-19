package com.compose.chi.presentation.screens.my_favourite_jokes_page

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.compose.chi.domain.use_case.DeleteAllJokesUseCase
import com.compose.chi.domain.use_case.ObserveLikedJokesUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MyFavouriteJokesViewModel(
    observeLikedJokesUseCase: ObserveLikedJokesUseCase,
    private val deleteAllJokes: DeleteAllJokesUseCase
): ViewModel() {

    val state = observeLikedJokesUseCase()
        .map { likedJokes ->
            MyFavouriteJokesState(jokes = likedJokes)
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            MyFavouriteJokesState()
        )

    fun deleteAllItemsFromDb() {
        viewModelScope.launch {
            deleteAllJokes()
        }
    }
}
