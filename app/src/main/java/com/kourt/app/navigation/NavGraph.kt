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
import com.kourt.app.ui.screens.auth.login.LoginScreen
import com.kourt.app.ui.screens.auth.register.RegisterScreen
import com.kourt.app.ui.screens.club.create.CreateClubScreen
import com.kourt.app.ui.screens.club.management.ClubManagementScreen
import com.kourt.app.ui.screens.club.review.ReviewConfirmScreen
import com.kourt.app.ui.screens.home.HomeScreen
import com.kourt.app.ui.screens.setup.SetupScreen
import com.kourt.app.ui.screens.team.create.AddTeamScreen

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

        composable(route = Destination.CreateClubScreen.route) {
            CreateClubScreen(navigation = navigation)
        }

        composable(route = Destination.ClubManagementScreen.route) {
            ClubManagementScreen(navigation = navigation)
        }

        composable(
            route = Destination.AddTeamScreen.route,
            arguments = listOf(
                navArgument("clubId") { type = NavType.StringType; defaultValue = "" },
            ),
        ) {
            AddTeamScreen(navigation = navigation)
        }

        composable(
            route = Destination.ReviewConfirmScreen.route,
            arguments = listOf(
                navArgument("clubName") { type = NavType.StringType; defaultValue = "" },
                navArgument("shortName") { type = NavType.StringType; defaultValue = "" },
                navArgument("president") { type = NavType.StringType; defaultValue = "" },
                navArgument("technicalDirector") { type = NavType.StringType; defaultValue = "" },
                navArgument("country") { type = NavType.StringType; defaultValue = "" },
                navArgument("city") { type = NavType.StringType; defaultValue = "" },
            ),
        ) {
            ReviewConfirmScreen(navigation = navigation)
        }

    }
}