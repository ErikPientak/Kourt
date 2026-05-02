package com.kourt.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import com.kourt.app.ui.screens.club.management.ClubMemberUiItem
import com.kourt.app.ui.screens.club.management.MemberFilter

@Composable
fun MembersSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        placeholder = {
            Text(
                text = stringResource(R.string.members_search_placeholder),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                modifier = Modifier.size(20.dp),
            )
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { keyboardController?.hide() }),
        shape = RoundedCornerShape(50.dp),
        textStyle = MaterialTheme.typography.bodyMedium,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.surface,
        ),
    )
}

@Composable
fun MembersFilterRow(
    activeFilter: MemberFilter,
    onFilterChange: (MemberFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    data class FilterOption(val filter: MemberFilter, val label: String)

    val options = listOf(
        FilterOption(MemberFilter.ALL, stringResource(R.string.members_filter_all)),
        FilterOption(MemberFilter.COACH, stringResource(R.string.members_filter_coach)),
        FilterOption(MemberFilter.PLAYER, stringResource(R.string.members_filter_player)),
        FilterOption(MemberFilter.ASSISTANT, stringResource(R.string.members_filter_assistant)),
        FilterOption(MemberFilter.PARENT, stringResource(R.string.members_filter_parent)),
    )

    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(options) { option ->
            val selected = activeFilter == option.filter
            FilterChip(
                selected = selected,
                onClick = { onFilterChange(option.filter) },
                label = {
                    Text(
                        text = option.label,
                        style = MaterialTheme.typography.labelMedium,
                    )
                },
                shape = RoundedCornerShape(50.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    containerColor = MaterialTheme.colorScheme.surface,
                    labelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selected,
                    borderColor = MaterialTheme.colorScheme.surface,
                    selectedBorderColor = MaterialTheme.colorScheme.primary,
                ),
            )
        }
    }
}

@Composable
fun MembersList(
    members: List<ClubMemberUiItem>,
    totalCount: Int,
    onMenuClick: (String) -> Unit,
    showMenu: Boolean = true,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(bottom = 88.dp), // clear FAB
    ) {
        item {
            Text(
                text = stringResource(R.string.members_count, totalCount),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            )
        }

        items(
            items = members,
            key = { it.memberId },
        ) { member ->
            MemberCard(
                playerName = member.displayName,
                role = member.role,
                teamName = member.subtitle,
                avatarUrl = member.avatarUrl,
                showMenu = showMenu,
                onMenuClick = if (showMenu) ({ onMenuClick(member.memberId) }) else null,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )
        }
    }
}

@Composable
fun MembersEmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(R.string.members_no_results),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = stringResource(R.string.members_no_results_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            )
        }
    }
}

@Composable
fun MembersErrorState(
    message: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}