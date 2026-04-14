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
    )
    fun navigateToClubManagementScreen()
    fun navigateToAddTeamScreen(clubId: String)
}