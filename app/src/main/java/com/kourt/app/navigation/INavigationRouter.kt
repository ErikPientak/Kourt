package com.kourt.app.navigation

import androidx.navigation.NavController

interface INavigationRouter {
    fun getNavController(): NavController
    fun returnBack()
    fun navigateToLoginScreen()
    fun navigateToRegisterScreen()
    fun navigateToHome()
    fun navigateToSetupScreen()
    fun navigateToCreateClubScreen()
    fun navigateToReviewConfirm(
        clubName: String,
        shortName: String,
        president: String,
        technicalDirector: String,
        country: String,
        city: String,
        accentColor: String,
        initials: String,
    )
    fun navigateToClubManagementScreen()
    fun navigateToAddTeamScreen(clubId: String)
    fun navigateToEditTeamScreen(teamId: String)
    fun navigateToSettingsScreen()
    fun navigateToProfileScreen()
    fun navigateToUserProfile(userId: String)
    fun navigateToEditProfile()
    fun navigateToDashboard()
    fun navigateToPlayerDashboard()
    fun navigateToAddEvent(teamId: String)
    fun navigateToEditEvent(eventId: String, teamId: String)
    fun navigateToEventDetail(eventId: String)
}