package com.kourt.app.navigation

import android.net.Uri
import androidx.navigation.NavController

class NavigationRouterImpl(private val navController: NavController) : INavigationRouter {
    override fun getNavController(): NavController = navController

    override fun returnBack() {
        navController.navigateUp()
    }

    override fun navigateToLoginScreen() {
        navController.navigate(Destination.LoginScreen.route)
    }

    override fun navigateToRegisterScreen() {
        navController.navigate(Destination.RegisterScreen.route)
    }

    override fun navigateToHome() {
        navController.navigate(Destination.HomeScreen.route)
    }

    override fun navigateToSetupScreen() {
        navController.navigate(Destination.SetupScreen.route)
    }

    override fun navigateToCreateClubScreen() {
        navController.navigate(Destination.CreateClubScreen.route)
    }

    override fun navigateToReviewConfirm(
        clubName: String,
        shortName: String,
        president: String,
        technicalDirector: String,
        country: String,
        city: String,
    ) {
        val route = "review_confirm" +
            "?clubName=${Uri.encode(clubName)}" +
            "&shortName=${Uri.encode(shortName)}" +
            "&president=${Uri.encode(president)}" +
            "&technicalDirector=${Uri.encode(technicalDirector)}" +
            "&country=${Uri.encode(country)}" +
            "&city=${Uri.encode(city)}"
        navController.navigate(route)
    }

    override fun navigateToClubManagementScreen() {
        navController.navigate(Destination.ClubManagementScreen.route)
    }

    override fun navigateToAddTeamScreen(clubId: String) {
        navController.navigate("add_team?clubId=${Uri.encode(clubId)}")
    }
}