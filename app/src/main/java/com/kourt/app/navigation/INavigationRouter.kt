package com.kourt.app.navigation

import androidx.navigation.NavController

interface INavigationRouter {
    fun getNavController(): NavController
    fun returnBack()
    fun navigateToLoginScreen()
    fun navigateToRegisterScreen()
    /**
     * @param clearBackStack true after signing in or registering, so Back can't return to
     * the auth screens; false when Setup is opened from Profile and should go back there.
     */
    fun navigateToSetupScreen(clearBackStack: Boolean = false)
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
    /** Club Management is a home screen, so the back stack is cleared like for the dashboards. */
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
    fun navigateToAttendance(eventId: String)
    fun navigateToNomination(eventId: String)
    fun navigateToAddChild()
    fun navigateToMatchStats(eventId: String)
    fun navigateToAfterMatchStats(eventId: String)
    fun navigateToAttendanceAnalytics()
    fun navigateToMatchStatsAnalytics()
}