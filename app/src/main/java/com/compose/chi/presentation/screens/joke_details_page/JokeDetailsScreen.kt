package com.compose.chi.presentation.screens.joke_details_page

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.compose.chi.domain.model.Joke
import com.compose.chi.presentation.navigation.components.AppTopAppBar
import com.compose.chi.presentation.ui.theme.CHITheme
import com.compose.chi.presentation.ui.theme.content_padding

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JokeDetailsScreen(
    navController: NavController,
    viewModel: JokeDetailsViewModel
) {
    val state by viewModel.state.collectAsState()

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    Scaffold(
        modifier = Modifier.fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            AppTopAppBar(
                title = if (state.joke != null) state.joke!!.punchline else "Loading Joke...",
                scrollBehavior = scrollBehavior,
                hasBackButton = true,
                onBackPressed = {
                    navController.popBackStack()
                },
                onSettingsPressed = {}
            )
        },
    ){ paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize()
                .padding(paddingValues)
        ) {
            state.joke?.let { joke ->
                JokeDetailsScreenContent(
                    joke = joke,
                    onLikeJoke = {
                        viewModel.toggleLikeJoke(joke)
                    },
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
                JokeDetailsScreenContent(joke = null, true, {})
            }
        }
    }
}


@Composable
private fun JokeDetailsScreenContent(
    joke: Joke?,
    isLoading: Boolean = false,
    onLikeJoke: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
            .background(Color.White),
        contentPadding = PaddingValues(content_padding)
    ) {
        item {
            Text(
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                text = "Joke Details",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
            Column(
                modifier = Modifier.fillMaxHeight().fillMaxWidth(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                if(isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                } else {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        text = "Joke: '${joke?.setup}'",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        text = "Punchline: '${joke?.punchline}'",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                IconButton(
                    onClick = onLikeJoke,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Icon(
                        imageVector = if(joke?.isFavourite == true)  Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Favourite"
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun JokeDetailsScreen() {
    CHITheme {
        val joke = Joke(punchline = "punchline", setup = "setup", type = "default", id = 1)
        JokeDetailsScreenContent(joke = joke, false, {})
    }
}