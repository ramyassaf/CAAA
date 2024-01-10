package com.compose.chi.presentation.screens.joke_home_page

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.compose.chi.ChiApplication
import com.compose.chi.common.Resource
import com.compose.chi.data.database.JokeDao
import com.compose.chi.data.database.model.toJokeEntity
import com.compose.chi.domain.model.Joke
import com.compose.chi.domain.use_case.GetJokeUseCase
import com.compose.chi.domain.use_case.IsJokeLikedUseCase
import com.compose.chi.presentation.helpers.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class JokeHomeViewModel(
    private val getJokeUseCase: GetJokeUseCase,
    private val isJokeLikedUseCase: IsJokeLikedUseCase,
    private val dao: JokeDao
) : ViewModel() {

    private val _state = MutableStateFlow(JokeHomeState())
    val state = _state.asStateFlow()

    init {
        getJoke()
    }

    fun getJoke() {
        getJokeUseCase().onEach { result ->
            when (result) {
                is Resource.Success -> {
                    result.data?.let { joke ->
                        // Collect the flow from IsJokeLikedUseCase and consume it's values each time it changes,
                        // then update _state with the new isFavourite value
                        isJokeLikedUseCase(joke.id).collect { isLiked ->
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

            dao.upsertJoke(jokeCopyFav.toJokeEntity())
        }
    }

    // ViewModel Factory in a companion object
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            val getJokeUseCase = GetJokeUseCase(ChiApplication.appModule.jokeRepository)
            val isJokeLikedUseCase = IsJokeLikedUseCase(ChiApplication.appModule.jokeRepository)
            val jokeDao: JokeDao = ChiApplication.appModule.db.dao
            JokeHomeViewModel(getJokeUseCase, isJokeLikedUseCase, jokeDao)
        }
    }
}