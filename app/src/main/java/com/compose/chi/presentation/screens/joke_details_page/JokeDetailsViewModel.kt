package com.compose.chi.presentation.screens.joke_details_page

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.compose.chi.common.Resource
import com.compose.chi.domain.model.Joke
import com.compose.chi.domain.use_case.GetJokeUseCase
import com.compose.chi.presentation.screens.joke_home_page.JokeHomeState
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class JokeDetailsViewModel(
    private val getJokeUseCase: GetJokeUseCase
): ViewModel() {

    private val _state = mutableStateOf(JokeHomeState())
    val state: State<JokeHomeState> = _state

    init {
        getJoke()
    }

    fun getJoke() {
        getJokeUseCase().onEach { result ->
            when (result) {
                is Resource.Success -> {
                    _state.value = JokeHomeState(joke = result.data)
                }
                is Resource.Error -> {
                    _state.value = JokeHomeState(
                        error = result.message ?: "An unexpected error occurred"
                    )
                }
                is Resource.Loading -> {
                    _state.value = JokeHomeState(isLoading = true)
                }
            }
        }.launchIn(viewModelScope)
    }
}