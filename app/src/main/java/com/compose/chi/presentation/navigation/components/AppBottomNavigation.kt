package com.compose.chi.presentation.navigation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBottomNavigation(
    items: List<BottomNavItem>,
    navController: NavController,
    onItemClick: (BottomNavItem) -> Unit
) {
    val backStackEntry = navController.currentBackStackEntryAsState()
    BottomNavigation(
        modifier = Modifier.height(72.dp),
        backgroundColor = MaterialTheme.colorScheme.primary,
        elevation = 5.dp
    ) {
        items.forEach { item ->
            val selected = item.route == backStackEntry.value?.destination?.route
            BottomNavigationItem(
                selected = selected,
                onClick = { onItemClick(item) },
                // TODO: Check if it's a bug in Material3 BottomNavigation (Since selectedContentColor, unselectedContentColor are not working)
                selectedContentColor = MaterialTheme.colorScheme.tertiary,
                unselectedContentColor = MaterialTheme.colorScheme.onPrimary,
                icon = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if(item.badgeCount > 0) {
                            BadgedBox(badge = {
                                Text(
                                    text = item.badgeCount.toString(),
                                    color = if (selected) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onPrimary
                                )
                            }) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.name,
                                    tint = if (selected) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        } else {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.name,
                                tint = if (selected) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onPrimary
                            )
                        }
                        if(selected) {
                            Text(
                                text = item.name,
                                textAlign = TextAlign.Center,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                }
            )
        }
    }
}