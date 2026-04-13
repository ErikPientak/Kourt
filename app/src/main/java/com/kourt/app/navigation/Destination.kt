package com.kourt.app.navigation

sealed class Destination(val route: String) {
    object LoginScreen    : Destination("login")
    object RegisterScreen : Destination("register")
    object HomeScreen     : Destination("home")
    object SetupScreen    : Destination("setup")
}
