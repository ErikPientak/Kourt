package com.kourt.app.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.kourt.app.ui.screens.setup.SetupScreenActions
import com.kourt.app.ui.screens.setup.SetupScreenUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

private const val TAG = "SetupViewModel"

@HiltViewModel
class SetupViewModel @Inject constructor() : ViewModel(), SetupScreenActions {

    var uiState by mutableStateOf(SetupScreenUiState())
        private set

    override fun onJoinCodeChange(code: String) {
        uiState = uiState.copy(joinCode = code)
    }

    override fun onCreateClub() {
        Log.d(TAG, "Create Club tapped")
    }

    override fun onJoinTeam() {
        Log.d(TAG, "Join Team tapped with code: ${uiState.joinCode}")
    }
}
