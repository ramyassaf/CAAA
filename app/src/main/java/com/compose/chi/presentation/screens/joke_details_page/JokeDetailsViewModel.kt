package com.compose.chi.presentation.screens.joke_details_page

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.compose.chi.common.Resource
import com.compose.chi.data.database.JokeDao
import com.compose.chi.data.database.model.toJokeEntity
import com.compose.chi.domain.model.Joke
import com.compose.chi.domain.use_case.GetJokeByIdUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class JokeDetailsViewModel(
    private val getJokeByIdUseCase: GetJokeByIdUseCase,
    private val dao: JokeDao,
    savedStateHandle: SavedStateHandle,
    private val jokeId: String
): ViewModel() {

    private val _state = MutableStateFlow(JokeDetailsState())
    val state = _state.asStateFlow()

    init {
        println("JokeDetailsViewModel()")

        println("jokeId= $jokeId")
        getJokeById(jokeId)
//        var jId: String = savedStateHandle.get<String>("jokeId") ?: "0"
//        println("jId= $jId")
//        savedStateHandle.get<String>("jokeId")?.let { jokeId ->
//            println("2 jokeId= $jokeId")
//            getJokeById(jokeId)
//        }
    }

    private fun getJokeById(jokeId: String) {
        getJokeByIdUseCase(jokeId).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    _state.update { JokeDetailsState(joke = result.data) }
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

    fun toggleLikeJoke(joke: Joke) {
        val isFavBeforeClick = joke.isFavourite
        val jokeCopyFav = joke.copy(isFavourite = !isFavBeforeClick)

        viewModelScope.launch {
            _state.update { JokeDetailsState(joke = jokeCopyFav) }

            dao.upsertJoke(jokeCopyFav.toJokeEntity())
        }
    }
}