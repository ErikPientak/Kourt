package com.kourt.app.data.repository.local

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

private const val PREFS_NAME = "app_preferences"
private const val KEY_DARK_THEME = "is_dark_theme"
private const val KEY_LANGUAGE = "language"
private const val KEY_ACTIVE_CLUB_ID = "active_club_id"
private const val KEY_ACTIVE_TEAM_ID = "active_team_id"
private const val KEY_ACTIVE_CHILD_ID = "active_child_id"
private const val KEY_LAST_START_ROUTE = "last_start_route"

/** Languages the app has strings for; anything else falls back to English. */
private val SUPPORTED_LANGUAGES = setOf("en", "cs")

/**
 * The language the user picked, or on first run the phone's language if the app supports it
 * (English otherwise). Takes a [Context] so MainActivity.attachBaseContext can call it before
 * Hilt has created the repository.
 */
fun savedAppLanguage(context: Context): String =
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getString(KEY_LANGUAGE, null)
        ?: defaultAppLanguage()

private fun defaultAppLanguage(): String =
    Locale.getDefault().language.takeIf { it in SUPPORTED_LANGUAGES } ?: "en"

@Singleton
open class AppPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var isDarkTheme: Boolean
        get() = prefs.getBoolean(KEY_DARK_THEME, true)
        set(value) = prefs.edit().putBoolean(KEY_DARK_THEME, value).apply()

    var language: String
        get() = prefs.getString(KEY_LANGUAGE, null) ?: defaultAppLanguage()
        set(value) = prefs.edit().putString(KEY_LANGUAGE, value).apply()

    var activeClubId: String
        get() = prefs.getString(KEY_ACTIVE_CLUB_ID, "") ?: ""
        set(value) = prefs.edit().putString(KEY_ACTIVE_CLUB_ID, value).apply()

    open var activeTeamId: String
        get() = prefs.getString(KEY_ACTIVE_TEAM_ID, "") ?: ""
        set(value) = prefs.edit().putString(KEY_ACTIVE_TEAM_ID, value).apply()

    var activeChildId: String
        get() = prefs.getString(KEY_ACTIVE_CHILD_ID, "") ?: ""
        set(value) = prefs.edit().putString(KEY_ACTIVE_CHILD_ID, value).apply()

    /** Name of the last resolved StartRoute, used when the app starts offline. */
    open var lastStartRoute: String
        get() = prefs.getString(KEY_LAST_START_ROUTE, "") ?: ""
        set(value) = prefs.edit().putString(KEY_LAST_START_ROUTE, value).apply()

    /** Forgets everything tied to the signed-in account; theme and language stay. */
    open fun clearSession() {
        prefs.edit()
            .remove(KEY_ACTIVE_CLUB_ID)
            .remove(KEY_ACTIVE_TEAM_ID)
            .remove(KEY_ACTIVE_CHILD_ID)
            .remove(KEY_LAST_START_ROUTE)
            .apply()
    }
}
