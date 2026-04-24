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
        accentColor: String,
        initials: String,
    ) {
        val route = "review_confirm" +
            "?clubName=${Uri.encode(clubName)}" +
            "&shortName=${Uri.encode(shortName)}" +
            "&president=${Uri.encode(president)}" +
            "&technicalDirector=${Uri.encode(technicalDirector)}" +
            "&country=${Uri.encode(country)}" +
            "&city=${Uri.encode(city)}" +
            "&accentColor=${Uri.encode(accentColor)}" +
            "&initials=${Uri.encode(initials)}"
        navController.navigate(route)
    }

    override fun navigateToClubManagementScreen() {
        navController.navigate(Destination.ClubManagementScreen.route)
    }

    override fun navigateToAddTeamScreen(clubId: String) {
        navController.navigate("add_team?clubId=${Uri.encode(clubId)}&teamId=")
    }

    override fun navigateToEditTeamScreen(teamId: String) {
        navController.navigate("add_team?clubId=&teamId=${Uri.encode(teamId)}")
    }

    override fun navigateToSettingsScreen() {
        navController.navigate(Destination.SettingsScreen.route)
    }

    override fun navigateToProfileScreen() {
        navController.navigate(Destination.ProfileScreen.route)
    }

    override fun navigateToUserProfile(userId: String) {
        navController.navigate("user_profile/${Uri.encode(userId)}")
    }

    override fun navigateToEditProfile() {
        // TODO: implement edit profile navigation
    }

    override fun navigateToDashboard() {
        navController.navigate(Destination.CoachDashboardScreen.route) {
            popUpTo(0) { inclusive = true }
        }
    }

    override fun navigateToAddEvent(teamId: String) {
        navController.navigate("add_edit_event?teamId=${Uri.encode(teamId)}&eventId=")
    }

    override fun navigateToEditEvent(eventId: String, teamId: String) {
        navController.navigate(
            "add_edit_event?teamId=${Uri.encode(teamId)}&eventId=${Uri.encode(eventId)}"
        )
    }
}