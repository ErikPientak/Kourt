package com.kourt.app.navigation

import androidx.navigation.NavController

interface INavigationRouter {
    fun getNavController(): NavController
    fun returnBack()
    fun navigateToLoginScreen()
    fun navigateToRegisterScreen()
    fun navigateToHome()
    fun navigateToSetupScreen()
}