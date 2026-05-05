package com.kourt.app.ui.screens.auth.login

import android.content.Context

interface LoginScreenActions {
    fun onSignInWithEmail(email: String, password: String)
    fun onSignInWithGoogle(context: Context)
    fun onClearError()
    fun onForgetPassword(email: String)
}
