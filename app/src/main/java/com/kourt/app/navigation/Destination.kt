package com.kourt.app.navigation

sealed class Destination(val route: String) {
    object LoginScreen        : Destination("login")
    object RegisterScreen     : Destination("register")
    object HomeScreen         : Destination("home")
    object SetupScreen        : Destination("setup")
    object CreateClubScreen   : Destination("create_club")
    object ReviewConfirmScreen : Destination(
        "review_confirm?clubName={clubName}&shortName={shortName}&president={president}" +
            "&technicalDirector={technicalDirector}&country={country}&city={city}" +
            "&accentColor={accentColor}&initials={initials}"
    )
    object ClubManagementScreen  : Destination("club_management")
    object CoachDashboardScreen  : Destination("coach_dashboard")
    object PlayerDashboardScreen : Destination("player_dashboard")
    object AddTeamScreen        : Destination("add_team?clubId={clubId}&teamId={teamId}")
    object SettingsScreen       : Destination("settings")
    object ProfileScreen        : Destination("profile")
    object UserProfileScreen    : Destination("user_profile/{userId}")
    object AddEditEventScreen   : Destination("add_edit_event?teamId={teamId}&eventId={eventId}")
    object EventDetailScreen    : Destination("event_detail/{eventId}")
}
