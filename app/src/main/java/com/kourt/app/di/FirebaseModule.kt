package com.kourt.app.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.kourt.app.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Host loopback as seen from the Android emulator. */
private const val EMULATOR_HOST = "10.0.2.2"
private const val AUTH_EMULATOR_PORT = 9099
private const val FIRESTORE_EMULATOR_PORT = 8080

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance().apply {
        if (BuildConfig.USE_FIREBASE_EMULATOR) useEmulator(EMULATOR_HOST, AUTH_EMULATOR_PORT)
    }

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance().apply {
        if (BuildConfig.USE_FIREBASE_EMULATOR) useEmulator(EMULATOR_HOST, FIRESTORE_EMULATOR_PORT)
    }
}
