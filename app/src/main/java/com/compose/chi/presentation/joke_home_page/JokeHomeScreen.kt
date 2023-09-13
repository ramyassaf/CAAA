package com.compose.chi.presentation.joke_home_page

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
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.compose.chi.domain.model.Joke
import com.compose.chi.presentation.ui.theme.*

@Composable
fun JokeHomeScreen(
    viewModel: JokeHomeViewModel
) {
    val state = viewModel.state.value

    Box(modifier = Modifier.fillMaxSize()) {
        state.joke?.let { joke ->
            JokeHomeScreenContent(
                joke = joke,
                onClickNewJoke = {
                    viewModel.getJoke()
                },
                onClick10NewJokes = {
                    //TODO: Navigate to 10 jokes page
                })
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
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }
}

@Composable
private fun JokeHomeScreenContent(
    joke: Joke,
    onClickNewJoke: () -> Unit,
    onClick10NewJokes: () -> Unit
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
                text = "Random Joke",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    text = "Joke: '${joke.setup}'",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    text = "Punchline: '${joke.punchline}'",
                    style = MaterialTheme.typography.bodyMedium
                )
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
        val joke = Joke(punchline = "punchline", setup = "setup")
        JokeHomeScreenContent(joke = joke, {}, {})
    }
}