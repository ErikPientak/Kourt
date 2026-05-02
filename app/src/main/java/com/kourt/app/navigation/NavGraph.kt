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
import com.kourt.app.ui.screens.settings.SettingsScreen
import com.kourt.app.ui.screens.settings.profile.ProfileScreen
import com.kourt.app.ui.screens.dashboard.coach.CoachDashboardScreen
import com.kourt.app.ui.screens.dashboard.player.PlayerDashboardScreen
import com.kourt.app.ui.screens.team.create.AddTeamScreen
import com.kourt.app.ui.screens.event.addedit.AddEditEventScreen
import com.kourt.app.ui.screens.event.attendance.AttendanceScreen
import com.kourt.app.ui.screens.event.detail.EventDetailScreen
import com.kourt.app.ui.screens.event.nomination.NominationScreen
import com.kourt.app.ui.screens.event.matchstats.MatchStatsScreen
import com.kourt.app.ui.screens.setup.addchild.AddChildScreen

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController(),
    navigation: INavigationRouter = remember { NavigationRouterImpl(navController) },
    startDestination: String,
    paddingValues: PaddingValues,
    isDarkTheme: Boolean = true,
    currentLanguage: String = "en",
    onToggleTheme: () -> Unit = {},
    onSetLanguage: (String) -> Unit = {},
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
            HomeScreen(
                navigation = navigation,
                isDarkTheme = isDarkTheme,
                currentLanguage = currentLanguage,
                onToggleTheme = onToggleTheme,
                onSetLanguage = onSetLanguage,
            )
        }

        composable(route = Destination.SetupScreen.route) {
            SetupScreen(navigation = navigation)
        }

        composable(route = Destination.AddChildScreen.route) {
            AddChildScreen(navigation = navigation)
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
                navArgument("teamId") { type = NavType.StringType; defaultValue = "" },
            ),
        ) {
            AddTeamScreen(navigation = navigation)
        }

        composable(route = Destination.SettingsScreen.route) {
            SettingsScreen(
                navigation = navigation,
                isDarkTheme = isDarkTheme,
                onToggleTheme = onToggleTheme,
                currentLanguage = currentLanguage,
                onSetLanguage = onSetLanguage,
            )
        }

        composable(route = Destination.ProfileScreen.route) {
            ProfileScreen(navigation = navigation)
        }

        composable(
            route = Destination.UserProfileScreen.route,
            arguments = listOf(
                navArgument("userId") { type = NavType.StringType; defaultValue = "" },
            ),
        ) {
            ProfileScreen(navigation = navigation)
        }

        composable(route = Destination.CoachDashboardScreen.route) {
            CoachDashboardScreen(navigation = navigation)
        }

        composable(route = Destination.PlayerDashboardScreen.route) {
            PlayerDashboardScreen(navigation = navigation)
        }

        composable(
            route = Destination.AddEditEventScreen.route,
            arguments = listOf(
                navArgument("teamId") { type = NavType.StringType; defaultValue = "" },
                navArgument("eventId") { type = NavType.StringType; defaultValue = "" },
            ),
        ) {
            AddEditEventScreen(navigation = navigation)
        }

        composable(
            route = Destination.EventDetailScreen.route,
            arguments = listOf(
                navArgument("eventId") { type = NavType.StringType; defaultValue = "" },
            ),
        ) {
            EventDetailScreen(navigation = navigation)
        }

        composable(
            route = Destination.AttendanceScreen.route,
            arguments = listOf(
                navArgument("eventId") { type = NavType.StringType; defaultValue = "" },
            ),
        ) {
            AttendanceScreen(navigation = navigation)
        }

        composable(
            route = Destination.NominationScreen.route,
            arguments = listOf(
                navArgument("eventId") { type = NavType.StringType; defaultValue = "" },
            ),
        ) {
            NominationScreen(navigation = navigation)
        }

        composable(
            route = Destination.MatchStatsScreen.route,
            arguments = listOf(
                navArgument("eventId") { type = NavType.StringType; defaultValue = "" },
            ),
        ) {
            MatchStatsScreen(navigation = navigation)
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
                navArgument("accentColor") { type = NavType.StringType; defaultValue = "#9CA3AF" },
                navArgument("initials") { type = NavType.StringType; defaultValue = "" },
            ),
        ) {
            ReviewConfirmScreen(navigation = navigation)
        }

    }
}