package com.kourt.app.viewmodel.club

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.kourt.app.data.model.Club
import com.kourt.app.data.repository.AuthRepository
import com.kourt.app.data.repository.ClubRepository
import com.kourt.app.ui.screens.club.review.ReviewConfirmScreenActions
import com.kourt.app.ui.screens.club.review.ReviewConfirmScreenUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "ReviewConfirmViewModel"
private const val JOIN_CODE_LENGTH = 6
private val JOIN_CODE_CHARS = ('A'..'Z') + ('0'..'9')

@HiltViewModel
class ReviewConfirmViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val clubRepository: ClubRepository,
    private val authRepository: AuthRepository,
) : ViewModel(), ReviewConfirmScreenActions {

    var uiState by mutableStateOf(
        ReviewConfirmScreenUiState(
            clubName = savedStateHandle.get<String>("clubName") ?: "",
            shortName = savedStateHandle.get<String>("shortName") ?: "",
            president = savedStateHandle.get<String>("president") ?: "",
            technicalDirector = savedStateHandle.get<String>("technicalDirector") ?: "",
            country = savedStateHandle.get<String>("country") ?: "",
            city = savedStateHandle.get<String>("city") ?: "",
        )
    )
        private set

    override fun onConfirm() {
        val uid = authRepository.currentUser?.uid
        if (uid == null) {
            uiState = uiState.copy(error = "You must be signed in to create a club")
            return
        }

        uiState = uiState.copy(isLoading = true, error = null)

        viewModelScope.launch {
            runCatching {
                val club = Club(
                    name = uiState.clubName.trim(),
                    shortName = uiState.shortName.trim(),
                    president = uiState.president.trim(),
                    technicalDirector = uiState.technicalDirector.trim(),
                    country = uiState.country,
                    city = uiState.city.trim(),
                    joinCode = generateJoinCode(),
                    createdBy = uid,
                    createdAt = Timestamp.now(),
                    logoURL = "",
                    adminIds = listOf(uid),
                )
                clubRepository.createClub(club)
            }.fold(
                onSuccess = { clubId ->
                    Log.d(TAG, "Club created with id=$clubId")
                    uiState = uiState.copy(isLoading = false, isSuccess = true)
                },
                onFailure = { e ->
                    Log.e(TAG, "Failed to create club", e)
                    uiState = uiState.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to create club",
                    )
                },
            )
        }
    }

    private fun generateJoinCode(): String =
        (1..JOIN_CODE_LENGTH).map { JOIN_CODE_CHARS.random() }.joinToString("")
}
