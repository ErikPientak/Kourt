package com.kourt.app.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kourt.app.ui.screens.auth.login.LoginScreen
import com.kourt.app.ui.screens.auth.register.RegisterScreen
import com.kourt.app.ui.screens.home.HomeScreen
import com.kourt.app.ui.screens.setup.SetupScreen

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController(),
    navigation: INavigationRouter = remember { NavigationRouterImpl(navController) },
    startDestination: String,
    paddingValues: PaddingValues
) {

    NavHost(
        navController = navController,
        startDestination = startDestination){

        composable(Destination.LoginScreen.route) {
            LoginScreen(navigation = navigation)
        }

        composable(route = Destination.RegisterScreen.route){
            RegisterScreen(navigation)
        }

        composable(route = Destination.HomeScreen.route) {
            HomeScreen(navigation = navigation)
        }

        composable(route = Destination.SetupScreen.route) {
            SetupScreen(navigation = navigation)
        }

    }
}