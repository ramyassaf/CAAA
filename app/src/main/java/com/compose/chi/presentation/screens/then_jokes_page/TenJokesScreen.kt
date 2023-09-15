package com.compose.chi.presentation.screens.then_jokes_page

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.compose.chi.presentation.navigation.Screen
import com.compose.chi.presentation.screens.then_jokes_page.components.JokeListItem

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TenJokesScreen(
    navController: NavController,
    viewModel: TenJokesViewModel
) {
    val state = viewModel.state.value
    Box(modifier = Modifier.fillMaxSize()) {

        if(state.jokes.isNotEmpty() || state.isLoading) {
            val ptrState = rememberPullRefreshState(state.isLoading, {viewModel.getTenJokes()}) // 1
            Box(modifier = Modifier
                .fillMaxSize()
                .pullRefresh(ptrState)) { // 2
                LazyColumn(
                    state = rememberLazyListState(),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(state.jokes) { joke ->
                        JokeListItem(
                            joke = joke,
                            onItemClick = {
                                navController.navigate(Screen.Home2Screen.route)
                            }
                        )
                    }
                }
                PullRefreshIndicator(state.isLoading, ptrState, Modifier.align(Alignment.TopCenter)) // 3
            }
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