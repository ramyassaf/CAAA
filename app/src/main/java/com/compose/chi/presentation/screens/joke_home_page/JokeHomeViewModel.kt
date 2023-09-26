package com.compose.chi.presentation.screens.joke_home_page

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.compose.chi.common.Resource
import com.compose.chi.data.database.JokeDao
import com.compose.chi.domain.model.Joke
import com.compose.chi.domain.model.toJokeEntity
import com.compose.chi.domain.use_case.GetJokeUseCase
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class JokeHomeViewModel(
    private val getJokeUseCase: GetJokeUseCase,
    private val dao: JokeDao
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

    fun toggleLikeJoke(joke: Joke) {
        val isFavBeforeClick = joke.isFavourite
        val jokeCopyFav = joke.copy(isFavourite = !isFavBeforeClick)
        
        viewModelScope.launch {
            _state.value = JokeHomeState(joke = jokeCopyFav)

            dao.upsertJoke(jokeCopyFav.toJokeEntity())
        }
    }
}