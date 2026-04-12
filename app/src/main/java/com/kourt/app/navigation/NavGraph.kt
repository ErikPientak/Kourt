package com.kourt.app.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kourt.app.ui.screens.auth.LoginScreen
import com.kourt.app.ui.screens.auth.RegisterScreen

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

    }
}