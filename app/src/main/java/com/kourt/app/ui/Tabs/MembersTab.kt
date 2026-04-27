package com.kourt.app.ui.Tabs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.kourt.app.R
import com.kourt.app.ui.components.MemberCard
import com.kourt.app.ui.components.MembersEmptyState
import com.kourt.app.ui.components.MembersErrorState
import com.kourt.app.ui.components.MembersFilterRow
import com.kourt.app.ui.components.MembersList
import com.kourt.app.ui.components.MembersSearchBar
import com.kourt.app.ui.screens.club.management.ClubManagementScreenUiState
import com.kourt.app.ui.screens.club.management.ClubMemberUiItem
import com.kourt.app.ui.screens.club.management.MemberFilter

@Composable
fun MembersTab(
    uiState: ClubManagementScreenUiState,
    onSearchQueryChange: (String) -> Unit,
    onFilterChange: (MemberFilter) -> Unit,
    onMenuClick: (String) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Search bar + filter chips are always visible so the user can clear
        // them even while the list is loading or in error state.
        MembersSearchBar(
            query = uiState.memberSearchQuery,
            onQueryChange = onSearchQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
        )

        MembersFilterRow(
            activeFilter = uiState.memberFilter,
            onFilterChange = onFilterChange,
            modifier = Modifier.fillMaxWidth(),
        )

        when {
            uiState.isMembersLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }

            uiState.membersError != null -> {
                MembersErrorState(
                    message = stringResource(uiState.membersError),
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                )
            }

            uiState.filteredMembers.isEmpty() -> {
                MembersEmptyState(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                )
            }

            else -> {
                MembersList(
                    members = uiState.filteredMembers,
                    totalCount = uiState.filteredMembers.size,
                    onMenuClick = onMenuClick,
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                )
            }
        }
    }
}


