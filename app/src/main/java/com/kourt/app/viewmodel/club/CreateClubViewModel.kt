package com.kourt.app.viewmodel.club

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.kourt.app.R
import com.kourt.app.ui.screens.club.create.CreateClubScreenActions
import com.kourt.app.ui.screens.club.create.CreateClubScreenUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

private const val TAG = "CreateClubViewModel"

@HiltViewModel
class CreateClubViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
) : ViewModel(), CreateClubScreenActions {

    var uiState by mutableStateOf(CreateClubScreenUiState())
        private set

    override fun onClubNameChange(value: String) {
        uiState = uiState.copy(clubName = value, error = null)
    }

    override fun onShortNameChange(value: String) {
        uiState = uiState.copy(shortName = value, error = null)
    }

    override fun onPresidentChange(value: String) {
        uiState = uiState.copy(president = value, error = null)
    }

    override fun onTechnicalDirectorChange(value: String) {
        uiState = uiState.copy(technicalDirector = value, error = null)
    }

    override fun onCountryChange(value: String) {
        uiState = uiState.copy(country = value)
    }

    override fun onCityChange(value: String) {
        uiState = uiState.copy(city = value, error = null)
    }

    override fun onCreateClub() {
        if (uiState.clubName.isBlank()) {
            uiState = uiState.copy(error = context.getString(R.string.error_club_name_required))
            return
        }
        // Validation passed — signal success so the screen navigates to ReviewConfirmScreen.
        // The actual Firestore write is performed by ReviewConfirmViewModel after the user
        // reviews and taps "Confirm & Create Club".
        uiState = uiState.copy(error = null, isSuccess = true)
        Log.d(TAG, "Validation passed, navigating to review screen")
    }

    override fun onSaveConsumed() {
        uiState = uiState.copy(isSuccess = false)
    }

    override fun onLogoUploadTap() {
        Log.d(TAG, "Logo upload tapped")
    }
}
