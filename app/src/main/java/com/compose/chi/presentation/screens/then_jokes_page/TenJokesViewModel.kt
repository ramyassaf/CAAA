package com.compose.chi.presentation.screens.then_jokes_page

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.compose.chi.common.Resource
import com.compose.chi.domain.use_case.GetTenJokesUseCase
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class TenJokesViewModel(
    private val getTenJokesUseCase: GetTenJokesUseCase
): ViewModel() {

    private val _state = mutableStateOf(TenJokesState())
    val state: State<TenJokesState> = _state

    init {
        getTenJokes()
    }

    private fun getTenJokes() {
        getTenJokesUseCase().onEach { result ->
            when (result) {
                is Resource.Success -> {
                    _state.value = TenJokesState(jokes = result.data ?: emptyList())
                }
                is Resource.Error -> {
                    _state.value = TenJokesState(
                        error = result.message ?: "An unexpected error occured"
                    )
                }
                is Resource.Loading -> {
                    _state.value = TenJokesState(isLoading = true)
                }
            }
        }.launchIn(viewModelScope)
    }
}