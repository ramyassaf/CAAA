package com.compose.chi.presentation.navigation.components

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopAppBar(
    title: String,
    scrollBehavior: TopAppBarScrollBehavior,
    hasBackButton: Boolean = false,
    onBackPressed: () -> Unit,
    onSettingsPressed: () -> Unit
) {

    TopAppBar(
        title = {
            Text(text = title)
        },
        navigationIcon = {
            if (hasBackButton) {
                IconButton(
                    onClick = onBackPressed
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Go back"
                    )
                }
            }
        },
        actions = {
            IconButton(
                onClick = onSettingsPressed
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings"
                )

            }
        },
        scrollBehavior = scrollBehavior
    )

}