package com.compose.chi.presentation.screens.my_favourite_jokes_page

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.compose.chi.domain.use_case.DeleteAllJokesUseCase
import com.compose.chi.domain.use_case.GetLikedJokesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MyFavouriteJokesViewModel(
    private val getLikedJokesUseCase: GetLikedJokesUseCase,
    private val deleteAllJokes: DeleteAllJokesUseCase
): ViewModel() {
    private val _allLikedJokes = getLikedJokesUseCase()

    private val _state = MutableStateFlow(MyFavouriteJokesState())
    val state = combine(_state, _allLikedJokes) { state, allLikedJokes ->
        state.copy(
            jokes = allLikedJokes,
            isLoading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MyFavouriteJokesState())

    fun deleteAllItemsFromDb() {
        viewModelScope.launch {
            deleteAllJokes()
        }
    }
}
