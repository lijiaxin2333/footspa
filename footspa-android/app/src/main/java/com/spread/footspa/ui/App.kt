package com.spread.footspa.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.spread.footspa.ui.card.CardScreen
import com.spread.footspa.ui.card.CardTypeManagementScreen
import com.spread.footspa.ui.statistics.IncomeScreen
import kotlinx.serialization.Serializable

sealed interface Route {
    @Serializable
    data object Main : Route

    @Serializable
    data object CardType : Route

    @Serializable
    data object Card : Route

    @Serializable
    data object Consume : Route

    @Serializable
    data object MassageService : Route

    @Serializable
    data object People : Route

    @Serializable
    data object Customer : Route

    @Serializable
    data object Income : Route
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
                startDestination = Route.Main
            ) {
                composable<Route.Main> {
                    MainScreen(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentSize()
                            .padding(horizontal = 5.dp),
                        navController = navController
                    )
                }
                composable<Route.CardType> {
                    CardTypeManagementScreen()
                }
                composable<Route.Card> {
                    CardScreen(modifier = Modifier.fillMaxSize())
                }
                composable<Route.Consume> {
                    ConsumeScreen()
                }
                composable<Route.MassageService> {
                    AddMassageService()
                }
                composable<Route.People> {
                    PeopleScreen(modifier = Modifier.fillMaxSize())
                }
                composable<Route.Customer> {
                    CustomerScreen(modifier = Modifier.fillMaxSize())
                }
                composable<Route.Income> {
                    IncomeScreen(modifier = Modifier.fillMaxSize())
                }
            }
        }
    }
}

@Composable
fun MainScreen(modifier: Modifier = Modifier, navController: NavController) {
    Column(modifier = modifier) {
        LazyVerticalGrid(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentSize(),
            columns = GridCells.Fixed(2)
        ) {
            item {
                MainButton(navController, "消费", Route.Consume)
            }
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = 5.dp))
        LazyVerticalGrid(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentSize(),
            columns = GridCells.Fixed(2)
        ) {
//            item {
//                MainButton(navController, "会员卡管理", Route.Card)
//            }
            item {
                MainButton(navController, "卡类管理", Route.CardType)
            }
            item {
                MainButton(navController, "项目管理", Route.MassageService)
            }
            item {
                MainButton(navController, "人员管理", Route.People)
            }
            item {
                MainButton(navController, "顾客管理", Route.Customer)
            }
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = 5.dp))
        LazyVerticalGrid(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentSize(),
            columns = GridCells.Fixed(2)
        ) {
            item {
                MainButton(navController, "收入数据", Route.Income)
            }
        }
    }
}

@Composable
fun MainButton(navController: NavController, text: String, route: Route) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .padding(horizontal = 5.dp, vertical = 5.dp), onClick = {
            navController.navigate(route)
        }, shape = RectangleShape
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Text(
                modifier = Modifier.align(Alignment.Center),
                textAlign = TextAlign.Center,
                text = text
            )
        }
    }
}