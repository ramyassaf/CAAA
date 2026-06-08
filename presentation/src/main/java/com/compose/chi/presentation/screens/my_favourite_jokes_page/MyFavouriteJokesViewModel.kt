package com.compose.chi.presentation.screens.my_favourite_jokes_page

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.compose.chi.domain.result.Resource
import com.compose.chi.domain.use_case.DeleteAllJokesUseCase
import com.compose.chi.domain.use_case.ObserveLikedJokesUseCase
import com.compose.chi.presentation.util.toUiMessage
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MyFavouriteJokesViewModel(
    observeLikedJokesUseCase: ObserveLikedJokesUseCase,
    private val deleteAllJokes: DeleteAllJokesUseCase
): ViewModel() {

    private val actionError = MutableStateFlow("")

    val state = observeLikedJokesUseCase()
        .map { likedJokesResult ->
            when (likedJokesResult) {
                is Resource.Success -> MyFavouriteJokesState(jokes = likedJokesResult.data)
                is Resource.Error -> MyFavouriteJokesState(
                    error = likedJokesResult.error.toUiMessage()
                )
            }
        }.combine(actionError) { likedJokesState, error ->
            if (error.isBlank()) {
                likedJokesState
            } else {
                likedJokesState.copy(error = error)
            }
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            MyFavouriteJokesState()
        )

    fun deleteAllItemsFromDb() {
        viewModelScope.launch {
            when (val result = deleteAllJokes()) {
                is Resource.Success -> actionError.value = ""
                is Resource.Error -> actionError.value = result.error.toUiMessage()
            }
        }
    }
}
