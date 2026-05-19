package com.compose.chi.presentation.screens.joke_home_page

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.compose.chi.common.Resource
import com.compose.chi.domain.model.Joke
import com.compose.chi.domain.use_case.GetJokeUseCase
import com.compose.chi.domain.use_case.ObserveJokeLikedStatusUseCase
import com.compose.chi.domain.use_case.UpsertJokeUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class JokeHomeViewModel(
    private val getJokeUseCase: GetJokeUseCase,
    private val observeJokeLikedStatusUseCase: ObserveJokeLikedStatusUseCase,
    private val upsertJokeUseCase: UpsertJokeUseCase
): ViewModel() {

    private val _state = MutableStateFlow(JokeHomeState())
    val state = _state.asStateFlow()

    private var getJokeJob: Job? = null

    init {
        getJoke()
    }

    fun getJoke() {
        getJokeJob?.cancel()
        getJokeJob = getJokeUseCase().onEach { result ->
            when (result) {
                is Resource.Success -> {
                    result.data?.let { joke ->
                        // Collect the flow from ObserveJokeLikedStatusUseCase and consume its values each time it changes,
                        // then update _state with the new isFavourite value
                        observeJokeLikedStatusUseCase(joke.id).collect { isLiked ->
                            if (isLiked) {
                                val jokeCopyFav = result.data.copy(isFavourite = true)
                                _state.update { JokeHomeState(joke = jokeCopyFav) }
                            } else {
                                _state.update { JokeHomeState(joke = result.data) }
                            }
                        }
                    }
                }

                is Resource.Error -> {
                    _state.update {
                        JokeHomeState(
                            error = result.message ?: "An unexpected error occurred"
                        )
                    }
                }

                is Resource.Loading -> {
                    _state.update { JokeHomeState(isLoading = true) }
                }
            }
        }.launchIn(viewModelScope)
    }

    fun toggleLikeJokeInDb(joke: Joke) {
        val isFavBeforeClick = joke.isFavourite
        val jokeCopyFav = joke.copy(isFavourite = !isFavBeforeClick)

        viewModelScope.launch {
            _state.update { JokeHomeState(joke = jokeCopyFav) }

            upsertJokeUseCase(jokeCopyFav)
        }
    }
}
