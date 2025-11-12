package com.spread.footspa.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.spread.footspa.ui.card.CardTypeManagementScreen
import kotlinx.serialization.Serializable

sealed interface Route {
    @Serializable
    data object Main : Route

    @Serializable
    data object CardType : Route
}

@Composable
fun App() {
    val navController = rememberNavController()
    Scaffold(
        modifier = Modifier.fillMaxSize(),
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavHost(
                navController = navController,
                startDestination = Route.CardType
            ) {
                composable<Route.CardType> {
                    CardTypeManagementScreen()
                }
            }
        }
    }
}