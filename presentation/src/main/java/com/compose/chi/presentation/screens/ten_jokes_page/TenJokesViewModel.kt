package com.compose.chi.presentation.screens.ten_jokes_page

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.compose.chi.domain.model.Joke
import com.compose.chi.domain.result.Resource
import com.compose.chi.domain.use_case.GetTenJokesUseCase
import com.compose.chi.presentation.util.toUiMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TenJokesViewModel(
    private val getTenJokesUseCase: GetTenJokesUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(TenJokesState())
    val state = _state.asStateFlow()

    private var _allJokes: List<Joke> = emptyList()

    init {
        getTenJokes()
    }

    fun getTenJokes() {
        if (_state.value.isLoading) return

        viewModelScope.launch {
            _state.update { TenJokesState(jokes = _allJokes, isLoading = true) }

            when (val result = getTenJokesUseCase()) {
                is Resource.Success -> {
                    _allJokes = result.data + _allJokes
                    _state.update { TenJokesState(jokes = _allJokes) }
                }

                is Resource.Error -> {
                    _state.update {
                        TenJokesState(
                            error = result.error.toUiMessage()
                        )
                    }
                }

            }
        }
    }
}
