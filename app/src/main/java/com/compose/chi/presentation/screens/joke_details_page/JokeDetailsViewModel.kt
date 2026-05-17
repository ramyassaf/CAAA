package com.compose.chi.presentation.screens.joke_details_page

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.compose.chi.ChiApplication
import com.compose.chi.common.Resource
import com.compose.chi.domain.model.Joke
import com.compose.chi.domain.use_case.GetJokeByIdUseCase
import com.compose.chi.domain.use_case.IsJokeLikedUseCase
import com.compose.chi.domain.use_case.UpsertJokeUseCase
import com.compose.chi.presentation.helpers.viewModelFactory
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class JokeDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val getJokeByIdUseCase: GetJokeByIdUseCase,
    private val isJokeLikedUseCase: IsJokeLikedUseCase,
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
        getJokeByIdJob = getJokeByIdUseCase(jokeId).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    result.data?.let { joke ->
                        // Collect the flow from IsJokeLikedUseCase and consume its values each time it changes,
                        // then update _state with the new isFavourite value
                        isJokeLikedUseCase(joke.id).collect { isLiked ->
                            if (isLiked) {
                                val jokeCopyFav = result.data.copy(isFavourite = true)
                                _state.update { JokeDetailsState(joke = jokeCopyFav) }
                            } else {
                                _state.update { JokeDetailsState(joke = result.data) }
                            }
                        }
                    }
                }

                is Resource.Error -> {
                    _state.update {
                        JokeDetailsState(
                            error = result.message ?: "An unexpected error occurred"
                        )
                    }
                }

                is Resource.Loading -> {
                    _state.update { JokeDetailsState(isLoading = true) }
                }
            }
        }.launchIn(viewModelScope)
    }

    fun toggleLikeJokeInDb(joke: Joke) {
        val isFavBeforeClick = joke.isFavourite
        val jokeCopyFav = joke.copy(isFavourite = !isFavBeforeClick)

        viewModelScope.launch {
            _state.update { JokeDetailsState(joke = jokeCopyFav) }

            upsertJokeUseCase(jokeCopyFav)
        }
    }

    // ViewModel Factory in a companion object
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            ChiApplication.appModule.jokeRepository.let { jokeRepo ->
                JokeDetailsViewModel(
                    it,
                    GetJokeByIdUseCase(jokeRepo),
                    IsJokeLikedUseCase(jokeRepo),
                    UpsertJokeUseCase(jokeRepo)
                )
            }
        }
    }
}