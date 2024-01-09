package com.compose.chi.presentation.screens.joke_details_page

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.compose.chi.ChiApplication
import com.compose.chi.common.Resource
import com.compose.chi.data.database.JokeDao
import com.compose.chi.data.database.model.toJokeEntity
import com.compose.chi.domain.model.Joke
import com.compose.chi.domain.use_case.GetJokeByIdUseCase
import com.compose.chi.presentation.helpers.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class JokeDetailsViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val getJokeByIdUseCase: GetJokeByIdUseCase,
    private val dao: JokeDao
): ViewModel() {

    private val _state = MutableStateFlow(JokeDetailsState())
    val state = _state.asStateFlow()

    init {
        savedStateHandle.get<String>("jokeId")?.let { jokeId ->
            getJokeById(jokeId)
        }
    }

    private fun getJokeById(jokeId: String) {
        getJokeByIdUseCase(jokeId).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    if (isJokeLiked(joke = result.data)) {
                        val jokeCopyFav = result.data?.copy(isFavourite = true)
                        _state.update { JokeDetailsState(joke = jokeCopyFav) }
                    } else {
                        _state.update { JokeDetailsState(joke = result.data) }
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

            dao.upsertJoke(jokeCopyFav.toJokeEntity())
        }
    }

    private suspend fun isJokeLiked(joke: Joke?): Boolean {
        if (joke == null) return false

        return suspendCoroutine { continuation ->
            viewModelScope.launch {
                val liked = dao.isFavoriteJoke(jokeId = joke.id)

                continuation.resume(liked)
            }
        }
    }

    // ViewModel Factory in a companion object
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            val getJokeByIdUseCase = GetJokeByIdUseCase(ChiApplication.appModule.jokeRepository)
            val jokeDao: JokeDao = ChiApplication.appModule.db.dao
            JokeDetailsViewModel(it, getJokeByIdUseCase, jokeDao)
        }
    }
}