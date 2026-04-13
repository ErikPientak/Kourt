package com.kourt.app.navigation

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
}