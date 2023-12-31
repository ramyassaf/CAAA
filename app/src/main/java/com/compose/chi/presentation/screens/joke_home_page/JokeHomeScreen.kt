package com.compose.chi.presentation.screens.joke_home_page

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.compose.chi.domain.model.Joke
import com.compose.chi.presentation.navigation.Screen
import com.compose.chi.presentation.navigation.components.AppTopAppBar
import com.compose.chi.presentation.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JokeHomeScreen(
    navController: NavController,
    viewModel: JokeHomeViewModel,
    onToggleDarkMode: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            AppTopAppBar(
                title = "Random Joke",
                scrollBehavior = scrollBehavior,
                hasBackButton = false,
                onBackPressed = {},
                onSettingsPressed = onToggleDarkMode
            )
        },
    ){ paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            state.joke?.let { joke ->
                JokeHomeScreenContent(
                    joke = joke,
                    onClickNewJoke = {
                        viewModel.getJoke()
                    },
                    onLikeJoke = {
                        viewModel.toggleLikeJokeInDb(joke)
                    },
                    onClick10NewJokes = {
                        navController.navigate(Screen.TenJokesScreen.route) {
                            // Support multiple back stacks
                            launchSingleTop = true
                            restoreState = true
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                        }
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
            if(state.isLoading) {
                JokeHomeScreenContent(joke = null, true,  {}, {}, {})
            }
        }
    }
}

@Composable
private fun JokeHomeScreenContent(
    joke: Joke?,
    isLoading: Boolean = false,
    onClickNewJoke: () -> Unit,
    onLikeJoke: () -> Unit,
    onClick10NewJokes: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(content_padding)
    ) {
        item {
            Text(
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                text = "Random Joke",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,

            )
            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                } else {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        text = "Joke: '${joke?.setup}'",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        text = "Punchline: '${joke?.punchline}'",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                IconButton(
                    onClick = onLikeJoke,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Icon(
                        imageVector = if (joke?.isFavourite == true) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        tint = MaterialTheme.colorScheme.error,
                        contentDescription = "Favourite"
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onClickNewJoke,
                    shape = ShapesRoundedCorner.large,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Get New Joke"
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onClick10NewJokes,
                    shape = ShapesRoundedCorner.large,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Get 10 New Jokes"
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun JokeHomeScreen() {
    CHITheme {
        val joke = Joke(punchline = "punchline", setup = "setup", type = "default", id = 1)
        JokeHomeScreenContent(joke = joke, false,  {}, {}, {})
    }
}

@Preview
@Composable
fun JokeHomeScreenDark() {
    CHITheme(
        darkTheme = true,
    ) {
        val joke = Joke(punchline = "punchline", setup = "setup", type = "default", id = 1)
        JokeHomeScreenContent(joke = joke, false,  {}, {}, {})
    }
}