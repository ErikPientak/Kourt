package com.kourt.app.data.repository.local

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

private const val PREFS_NAME = "app_preferences"
private const val KEY_DARK_THEME = "is_dark_theme"
private const val KEY_LANGUAGE = "language"
private const val KEY_ACTIVE_CLUB_ID = "active_club_id"
private const val KEY_ACTIVE_TEAM_ID = "active_team_id"
private const val KEY_ACTIVE_CHILD_ID = "active_child_id"

@Singleton
open class AppPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var isDarkTheme: Boolean
        get() = prefs.getBoolean(KEY_DARK_THEME, true)
        set(value) = prefs.edit().putBoolean(KEY_DARK_THEME, value).apply()

    var language: String
        get() = prefs.getString(KEY_LANGUAGE, "en") ?: "en"
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
}
