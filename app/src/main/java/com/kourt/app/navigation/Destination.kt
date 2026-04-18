package com.kourt.app.navigation

sealed class Destination(val route: String) {
    object LoginScreen        : Destination("login")
    object RegisterScreen     : Destination("register")
    object HomeScreen         : Destination("home")
    object SetupScreen        : Destination("setup")
    object CreateClubScreen   : Destination("create_club")
    object ReviewConfirmScreen : Destination(
        "review_confirm?clubName={clubName}&shortName={shortName}&president={president}" +
            "&technicalDirector={technicalDirector}&country={country}&city={city}"
    )
    object ClubManagementScreen : Destination("club_management")
    object AddTeamScreen        : Destination("add_team?clubId={clubId}&teamId={teamId}")
    object SettingsScreen       : Destination("settings")
}
