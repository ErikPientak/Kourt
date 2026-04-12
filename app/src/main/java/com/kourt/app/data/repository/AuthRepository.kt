package com.kourt.app.data.repository

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.kourt.app.R
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth
) {
    val currentUser: FirebaseUser?
        get() = auth.currentUser

    val isLoggedIn: Boolean
        get() = auth.currentUser != null

    suspend fun signInWithEmail(email: String, password: String): Result<FirebaseUser> = runCatching {
        auth.signInWithEmailAndPassword(email, password).await().user!!
    }

    suspend fun signUpWithEmail(email: String, password: String): Result<FirebaseUser> = runCatching {
        auth.createUserWithEmailAndPassword(email, password).await().user!!
    }

    suspend fun signInWithGoogle(context: Context): Result<FirebaseUser> = runCatching {
        val credentialManager = CredentialManager.create(context)

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(context.getString(R.string.web_client_id))
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val result = try {
            credentialManager.getCredential(context, request)
        } catch (e: GetCredentialException) {
            throw e
        }

        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(result.credential.data)
        val firebaseCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
        auth.signInWithCredential(firebaseCredential).await().user!!
    }

    fun signOut() {
        auth.signOut()
    }
}
