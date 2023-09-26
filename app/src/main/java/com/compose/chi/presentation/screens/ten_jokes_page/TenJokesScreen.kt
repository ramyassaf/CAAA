package com.compose.chi.presentation.screens.ten_jokes_page

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.PullRefreshState
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.compose.chi.domain.model.Joke
import com.compose.chi.presentation.navigation.Screen
import com.compose.chi.presentation.navigation.components.AppTopAppBar
import com.compose.chi.presentation.screens.ten_jokes_page.components.JokeListItem
import com.compose.chi.presentation.ui.theme.CHITheme

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TenJokesScreen(
    navController: NavController,
    viewModel: TenJokesViewModel
) {
    val state = viewModel.state.value

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            AppTopAppBar(
                title = "Random 10 Jokes",
                scrollBehavior = scrollBehavior,
                hasBackButton = false,
                onBackPressed = {
                    navController.popBackStack()
                },
                onSettingsPressed = {}
            )
        },
    ){ paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if(state.jokes.isNotEmpty() || state.isLoading) {
                val ptrState = rememberPullRefreshState(state.isLoading, {viewModel.getTenJokes()}) // 1
                TenJokesScreenContent(
                    jokes = state.jokes,
                    ptrState = ptrState,
                    onClickNewJoke = {
                        navController.navigate(Screen.JokeDetails.route)
                    }
                )
            }

            if(state.error.isNotBlank()) {
                Text(
                    text = state.error,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .align(Alignment.Center)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun TenJokesScreenContent(
    jokes: List<Joke>,
    isLoading: Boolean = false,
    ptrState: PullRefreshState,
    onClickNewJoke: () -> Unit
) {
    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color.White)
        .pullRefresh(ptrState)) { // 2
        LazyColumn(
            state = rememberLazyListState(),
            modifier = Modifier.fillMaxSize()
        ) {
            items(jokes) { joke ->
                JokeListItem(
                    joke = joke,
                    onItemClick = {
                        onClickNewJoke()
                    }
                )
            }
        }
        PullRefreshIndicator(isLoading, ptrState, Modifier.align(Alignment.TopCenter)) // 3
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Preview
@Composable
fun TenJokesScreen() {
    CHITheme {
        val jokes = mutableListOf<Joke>()
        for (i in 1..50) {
            val joke = Joke(punchline = "punchline $i", setup = "setup $i", type = "default", id = i)
            jokes.add(joke)
        }
        val ptrState = rememberPullRefreshState(false, {}) // 1
        TenJokesScreenContent(jokes = jokes, false,  ptrState, {})
    }
}