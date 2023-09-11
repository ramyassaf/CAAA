package com.compose.chi.presentation.joke_home_page

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.compose.chi.domain.model.Joke
import com.compose.chi.presentation.ui.theme.CHITheme

@Composable
fun JokeHomeScreen(
    viewModel: JokeHomeViewModel
) {
    val state = viewModel.state.value

    Box(modifier = Modifier.fillMaxSize()) {
        state.joke?.let { joke ->
            JokeHomeScreenContent(joke = joke, onGetNewJoke = {
                viewModel.getJoke()
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
    onGetNewJoke: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp)
    ) {
        item {
            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Joke: '${joke.setup}'",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Joke: '${joke.punchline}'",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onGetNewJoke,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "New Joke"
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
        JokeHomeScreenContent(joke = joke, {})
    }
}