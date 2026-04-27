package com.kourt.app.ui.Tabs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import com.kourt.app.R
import com.kourt.app.ui.components.EventCard
import com.kourt.app.ui.screens.club.management.ClubManagementScreenActions
import com.kourt.app.ui.screens.club.management.ClubManagementScreenUiState
import com.kourt.app.ui.screens.club.management.EventStatusFilter
import com.kourt.app.ui.screens.dashboard.coach.EventUiItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsTab(
    uiState: ClubManagementScreenUiState,
    actions: ClubManagementScreenActions,
    onEditEvent: (eventId: String, teamId: String) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        EventStatusFilterRow(
            activeFilter = uiState.eventStatusFilter,
            onFilterChange = actions::onEventStatusFilterChange,
            modifier = Modifier.padding(vertical = 8.dp),
        )

        when {
            uiState.isEventsLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
            uiState.filteredEvents.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(R.string.events_tab_empty),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    )
                }
            }
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 24.dp),
                ) {
                    items(items = uiState.filteredEvents, key = { it.eventId }) { event ->
                        val badgeColor = remember(event.teamColor) {
                            runCatching { Color(event.teamColor.toColorInt()) }
                                .getOrDefault(Color(0xFF9CA3AF))
                        }
                        EventCard(
                            title = event.title,
                            eventType = event.type,
                            status = event.status,
                            epochDay = event.epochDay,
                            dayOfWeek = event.dayOfWeek,
                            dayOfMonth = event.dayOfMonth,
                            startTime = event.startTime,
                            location = event.location,
                            teamBadge = event.teamName.ifBlank { null },
                            teamBadgeColor = badgeColor,
                            showChevron = false,
                            showMenu = true,
                            onMenuClick = { actions.onEventMenuClick(event.eventId) },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        )
                    }
                }
            }
        }
    }

    // ── Event action bottom sheet ─────────────────────────────────────────────
    val selectedEvent = uiState.selectedEvent
    if (selectedEvent != null) {
        EventActionBottomSheet(
            event = selectedEvent,
            onDismiss = actions::onEventActionDismiss,
            onEdit = { onEditEvent(selectedEvent.eventId, selectedEvent.teamId) },
            onCancel = { actions.onEventCancelClick(selectedEvent.eventId) },
            onDelete = { actions.onEventDeleteClick(selectedEvent.eventId) },
        )
    }

    // ── Cancel confirmation dialog ────────────────────────────────────────────
    val eventForCancel = uiState.eventForCancel
    if (eventForCancel != null) {
        AlertDialog(
            onDismissRequest = actions::onEventCancelDismiss,
            title = { Text(stringResource(R.string.cancel_event_title)) },
            text = { Text(stringResource(R.string.cancel_event_message)) },
            confirmButton = {
                TextButton(onClick = { actions.onEventCancelConfirm(eventForCancel.eventId) }) {
                    Text(
                        text = stringResource(R.string.cancel_event_confirm),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = actions::onEventCancelDismiss) {
                    Text(stringResource(R.string.cancel_event_keep))
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
        )
    }

    // ── Delete confirmation dialog ────────────────────────────────────────────
    val eventForDelete = uiState.eventForDelete
    if (eventForDelete != null) {
        AlertDialog(
            onDismissRequest = actions::onEventDeleteDismiss,
            title = { Text(stringResource(R.string.delete_event_title)) },
            text = { Text(stringResource(R.string.delete_event_message)) },
            confirmButton = {
                TextButton(onClick = { actions.onEventDeleteConfirm(eventForDelete.eventId) }) {
                    Text(
                        text = stringResource(R.string.delete_event_confirm),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = actions::onEventDeleteDismiss) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
        )
    }
}

// ── Event action bottom sheet ─────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EventActionBottomSheet(
    event: EventUiItem,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onCancel: () -> Unit,
    onDelete: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 8.dp),
        ) {
            Text(
                text = event.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
            )

            EventActionRow(
                icon = painterResource(R.drawable.edit),
                label = stringResource(R.string.event_action_edit),
                contentColor = MaterialTheme.colorScheme.onSurface,
                onClick = { onEdit(); onDismiss() },
            )

            if (event.status != "cancelled") {
                EventActionRow(
                    icon = painterResource(R.drawable.cancel),
                    label = stringResource(R.string.event_action_cancel),
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    onClick = { onCancel(); onDismiss() },
                )
            }

            EventActionRow(
                icon = painterResource(R.drawable.delete),
                label = stringResource(R.string.event_action_delete),
                contentColor = MaterialTheme.colorScheme.error,
                onClick = { onDelete(); onDismiss() },
            )
        }
    }
}

@Composable
private fun EventActionRow(
    icon: Painter,
    label: String,
    contentColor: Color,
    onClick: () -> Unit,
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(22.dp),
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = contentColor,
            )
        }
    }
}

// ── Filter chips ──────────────────────────────────────────────────────────────

@Composable
private fun EventStatusFilterRow(
    activeFilter: EventStatusFilter,
    onFilterChange: (EventStatusFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    data class FilterOption(val filter: EventStatusFilter, val label: String)

    val options = listOf(
        FilterOption(EventStatusFilter.ALL, stringResource(R.string.event_filter_all)),
        FilterOption(EventStatusFilter.UPCOMING, stringResource(R.string.event_filter_upcoming)),
        FilterOption(EventStatusFilter.PAST, stringResource(R.string.event_filter_past)),
        FilterOption(EventStatusFilter.CANCELLED, stringResource(R.string.event_filter_cancelled)),
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
