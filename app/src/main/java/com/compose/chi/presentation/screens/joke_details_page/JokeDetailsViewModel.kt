package com.compose.chi.presentation.screens.joke_details_page

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import com.compose.chi.common.Resource
import com.compose.chi.data.database.JokeDao
import com.compose.chi.domain.use_case.GetJokeByIdUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

@OptIn(SavedStateHandleSaveableApi::class)

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
        getJokeById(jokeId.toInt())
//        savedStateHandle.get<String>("jokeId")?.let { jokeId ->
//            getJokeById(jokeId.toInt())
//        }
    }

    fun getJokeById(jokeId: Int) {
        getJokeByIdUseCase(jokeId).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    _state.update { JokeDetailsState(joke = result.data) }
                }
                is Resource.Error -> {
                    JokeDetailsState(
                        error = result.message ?: "An unexpected error occurred"
                    )
                }
                is Resource.Loading -> {
                    _state.update { JokeDetailsState(isLoading = true) }
                }
            }
        }.launchIn(viewModelScope)
    }
}