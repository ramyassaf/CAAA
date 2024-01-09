package com.compose.chi.presentation.screens.my_favourite_jokes_page

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyFavouriteJokesScreen(
    navController: NavController,
    viewModel: MyFavouriteJokesViewModel
) {
    val state by viewModel.state.collectAsState()

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            AppTopAppBar(
                title = "My Favourite Jokes 💖",
                scrollBehavior = scrollBehavior,
                hasBackButton = false,
                onBackPressed = {
                    navController.popBackStack()
                },
                onSettingsPressed = {}
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.deleteAllItemsFromDb()
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete All records"
                )
            }
        },
    ){ paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if(state.jokes.isNotEmpty() || state.isLoading) {
                MyFavouriteJokesScreenContent(
                    jokes = state.jokes,
                    onSelectItem = {
//                        navController.navigate(Screen.JokeDetails.route)
                        navController.navigate(Screen.JokeDetails.route + "/${it}")
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
private fun MyFavouriteJokesScreenContent(
    jokes: List<Joke>,
    onSelectItem: (jokeId: Int) -> Unit
) {
    Box(modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
    ) { // 2
        LazyColumn(
            state = rememberLazyListState(),
            modifier = Modifier.fillMaxSize()
        ) {
            items(jokes) { joke ->
                JokeListItem(
                    joke = joke,
                    onItemClick = {
                        onSelectItem(it.id)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Preview
@Composable
fun MyFavouriteJokesScreen() {
    CHITheme {
        val jokes = mutableListOf<Joke>()
        for (i in 1..50) {
            val joke = Joke(punchline = "punchline $i", setup = "setup $i", type = "default", id = i)
            jokes.add(joke)
        }
        val ptrState = rememberPullRefreshState(false, {}) // 1
        MyFavouriteJokesScreenContent(jokes = jokes, {})
    }
}