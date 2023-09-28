package com.compose.chi.presentation.screens.ten_jokes_page

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.compose.chi.common.Resource
import com.compose.chi.domain.model.Joke
import com.compose.chi.domain.use_case.GetTenJokesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

class TenJokesViewModel(
    private val getTenJokesUseCase: GetTenJokesUseCase,
): ViewModel() {

    private val _state = MutableStateFlow(TenJokesState())
    val state = _state.asStateFlow()

    private var _allJokes: List<Joke> = emptyList()

    init {
        getTenJokes()
    }

    fun getTenJokes() {
        getTenJokesUseCase().onEach { result ->
            when (result) {
                is Resource.Success -> {
                    _allJokes = (result.data ?: emptyList()) + _allJokes
                    _state.update { TenJokesState(jokes = _allJokes) }
                }
                is Resource.Error -> {
                    _state.update {
                        TenJokesState(
                            error = result.message ?: "An unexpected error occurred"
                        )
                    }
                }
                is Resource.Loading -> {
                    _state.update { TenJokesState(jokes = _allJokes, isLoading = true) }
                }
            }
        }.launchIn(viewModelScope)
    }
}