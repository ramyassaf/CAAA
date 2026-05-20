package com.compose.chi.presentation.screens.joke_details_page

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.compose.chi.domain.model.Joke
import com.compose.chi.domain.result.Resource
import com.compose.chi.domain.use_case.GetJokeByIdUseCase
import com.compose.chi.domain.use_case.ObserveJokeLikedStatusUseCase
import com.compose.chi.domain.use_case.UpsertJokeUseCase
import com.compose.chi.presentation.util.toUiMessage
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class JokeDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val getJokeByIdUseCase: GetJokeByIdUseCase,
    private val observeJokeLikedStatusUseCase: ObserveJokeLikedStatusUseCase,
    private val upsertJokeUseCase: UpsertJokeUseCase
): ViewModel() {

    private val _state = MutableStateFlow(JokeDetailsState())
    val state = _state.asStateFlow()

    private var getJokeByIdJob: Job? = null

    init {
        savedStateHandle.get<String>("jokeId")?.let { jokeId ->
            getJokeById(jokeId)
        }
    }

    private fun getJokeById(jokeId: String) {
        getJokeByIdJob?.cancel()
        getJokeByIdJob = viewModelScope.launch {
            _state.update { JokeDetailsState(isLoading = true) }

            when (val result = getJokeByIdUseCase(jokeId)) {
                is Resource.Success -> {
                    val joke = result.data
                    // Collect the flow from ObserveJokeLikedStatusUseCase and consume its values each time it changes,
                    // then update _state with the new isFavourite value
                    observeJokeLikedStatusUseCase(joke.id).collect { likedStatusResult ->
                        when (likedStatusResult) {
                            is Resource.Success -> {
                                if (likedStatusResult.data) {
                                    val jokeCopyFav = joke.copy(isFavourite = true)
                                    _state.update { JokeDetailsState(joke = jokeCopyFav) }
                                } else {
                                    _state.update { JokeDetailsState(joke = joke) }
                                }
                            }

                            is Resource.Error -> {
                                _state.update {
                                    it.copy(error = likedStatusResult.error.toUiMessage())
                                }
                            }
                        }
                    }
                }

                is Resource.Error -> {
                    _state.update {
                        JokeDetailsState(
                            error = result.error.toUiMessage()
                        )
                    }
                }

            }
        }
    }

    fun toggleLikeJokeInDb(joke: Joke) {
        val isFavBeforeClick = joke.isFavourite
        val jokeCopyFav = joke.copy(isFavourite = !isFavBeforeClick)

        viewModelScope.launch {
            _state.update { JokeDetailsState(joke = jokeCopyFav) }

            when (val result = upsertJokeUseCase(jokeCopyFav)) {
                is Resource.Success -> Unit
                is Resource.Error -> {
                    _state.update { it.copy(error = result.error.toUiMessage()) }
                }
            }
        }
    }
}
