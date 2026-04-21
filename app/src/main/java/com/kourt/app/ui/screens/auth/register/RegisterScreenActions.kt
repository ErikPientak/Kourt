package com.kourt.app.ui.screens.auth.register

interface RegisterScreenActions {
    fun onRegister(fullName: String, email: String, password: String, confirmPassword: String)
    fun onClearError()
    fun onSaveConsumed()
}
