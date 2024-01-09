package com.compose.chi.presentation.navigation.components

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
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
        // TODO: Check if it's a bug in Material3 TopAppBar (Since titleContentColor, navigationIconContentColor, actionIconContentColor are not working)
        // Customize Colors here
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary
        ),
        title = {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onPrimary
            )
        },
        navigationIcon = {
            if (hasBackButton) {
                IconButton(
                    onClick = onBackPressed
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Go back",
                        tint = MaterialTheme.colorScheme.onPrimary
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
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.onPrimary
                )

            }
        },
        scrollBehavior = scrollBehavior
    )

}