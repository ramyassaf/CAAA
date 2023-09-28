package com.compose.chi.presentation.screens.joke_details_page

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.compose.chi.common.Resource
import com.compose.chi.domain.use_case.GetJokeUseCase
import com.compose.chi.presentation.screens.joke_home_page.JokeHomeState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

class JokeDetailsViewModel(
    private val getJokeUseCase: GetJokeUseCase
): ViewModel() {

    private val _state = MutableStateFlow(JokeHomeState())
    val state = _state.asStateFlow()

    init {
        getJoke()
    }

    fun getJoke() {
        getJokeUseCase().onEach { result ->
            when (result) {
                is Resource.Success -> {
                    _state.update { JokeHomeState(joke = result.data) }
                }
                is Resource.Error -> {
                    JokeHomeState(
                        error = result.message ?: "An unexpected error occurred"
                    )
                }
                is Resource.Loading -> {
                    _state.update { JokeHomeState(isLoading = true) }
                }
            }
        }.launchIn(viewModelScope)
    }
}