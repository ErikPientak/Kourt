package com.kourt.app.ui.screens.event.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kourt.app.R
import com.kourt.app.navigation.Destination
import com.kourt.app.navigation.INavigationRouter
import com.kourt.app.ui.components.BaseScreen
import com.kourt.app.ui.components.KourtAvatarLeading
import com.kourt.app.ui.components.bottomSheets.RsvpReasonBottomSheet
import com.kourt.app.ui.components.bottomSheets.RsvpSelectionBottomSheet
import com.kourt.app.ui.theme.KourtTheme
import com.kourt.app.viewmodel.event.EventDetailViewModel

private val LineupAccent = Color(0xFFF97316)

@Composable
fun EventDetailScreen(
    navigation: INavigationRouter,
    viewModel: EventDetailViewModel = hiltViewModel(),
) {
    val uiState = viewModel.uiState
    val navBackStackEntry by navigation.getNavController().currentBackStackEntryAsState()

    LaunchedEffect(navBackStackEntry) {
        if (navBackStackEntry?.destination?.route == Destination.EventDetailScreen.route) {
            viewModel.loadEventDetail()
        }
    }

    EventDetailContent(
        uiState = uiState,
        actions = viewModel,
        onBack = { navigation.returnBack() },
        onEdit = { uiState.event?.let { navigation.navigateToEditEvent(it.eventId, it.teamId) } },
    )
}

@Composable
private fun EventDetailContent(
    uiState: EventDetailScreenUiState,
    actions: EventDetailScreenActions,
    onBack: () -> Unit,
    onEdit: () -> Unit,
) {
    BaseScreen(
        title = stringResource(R.string.event_detail_title),
        onBack = onBack,
        trailingIcon = if (uiState.isEditable) {
            {
                IconButton(onClick = onEdit) {
                    Icon(
                        painter = painterResource(R.drawable.edit),
                        contentDescription = stringResource(R.string.event_detail_edit_cd),
                        tint = MaterialTheme.colorScheme.onBackground,
                    )
                }
            }
        } else null,
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary,
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    uiState.event?.let { event ->
                        EventInfoSection(event = event)
                    }

                    if (uiState.event?.type?.lowercase() == "match" && uiState.nominatedPlayers.isNotEmpty()) {
                        NominatedPlayersSection(players = uiState.nominatedPlayers)
                    }

                    if (uiState.canRsvp) {
                        YourStatusSection(uiState = uiState, actions = actions)
                    }

                    if (uiState.goingMembers.isNotEmpty() || uiState.rsvpYes + uiState.rsvpLate > 0) {
                        GoingSection(uiState = uiState, actions = actions)
                    }

                    if (uiState.notGoingMembers.isNotEmpty() || uiState.rsvpNo > 0) {
                        NotGoingSection(uiState = uiState, actions = actions)
                    }

                    uiState.error?.let { errorRes ->
                        Text(
                            text = stringResource(errorRes),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
        }

        // Bottom sheets
        if (uiState.isRsvpSheetOpen) {
            RsvpSelectionBottomSheet(
                onRsvpSelected = actions::onRsvpSelected,
                onDismiss = actions::onDismissRsvpSheet,
            )
        }

        if (uiState.isRsvpReasonSheetOpen) {
            RsvpReasonBottomSheet(
                selectedReason = uiState.selectedReason,
                reasonNote = uiState.reasonNote,
                isSubmitting = uiState.isRsvpSubmitting,
                onReasonSelected = actions::onRsvpReasonSelected,
                onNoteChanged = actions::onRsvpNoteChanged,
                onSubmitReason = actions::onSubmitRsvp,
                onDismiss = actions::onDismissReasonSheet,
            )
        }
    }
}

// ── Section: Event Info ───────────────────────────────────────────────────────

@Composable
private fun EventInfoSection(event: EventDetailUiItem) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionLabel(text = stringResource(R.string.event_detail_section_info))

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.DateRange,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                    Text(
                        text = event.formattedDate,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                if (event.location.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.location),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp),
                        )
                        Text(
                            text = event.location,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }

                if (event.notes.isNotBlank()) {
                    Text(
                        text = event.notes,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    )
                }
            }
        }
    }
}

// ── Section: Lineup ──────────────────────────────────────────────────────────

@Composable
private fun NominatedPlayersSection(players: List<NominatedPlayerUiItem>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionLabelWithBadge(
            text = stringResource(R.string.event_detail_section_lineup),
            count = players.size,
            badgeColor = LineupAccent,
        )

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column {
                players.forEachIndexed { index, player ->
                    if (index > 0) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.background,
                            thickness = 1.dp,
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        if (player.jerseyNumber > 0) {
                            Surface(
                                shape = CircleShape,
                                color = LineupAccent,
                                modifier = Modifier.size(32.dp),
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = player.jerseyNumber.toString(),
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White,
                                    )
                                }
                            }
                        } else {
                            Spacer(modifier = Modifier.size(32.dp))
                        }
                        KourtAvatarLeading(
                            fallbackText = player.displayName,
                            photoUrl = player.avatarUrl,
                        )
                        Text(
                            text = player.displayName,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

// ── Section: Your Status ─────────────────────────────────────────────────────

@Composable
private fun YourStatusSection(
    uiState: EventDetailScreenUiState,
    actions: EventDetailScreenActions,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionLabel(text = stringResource(R.string.event_detail_section_your_status))

        // Status card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RsvpStatusIcon(status = uiState.myRsvpStatus)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = rsvpStatusTitle(uiState.myRsvpStatus),
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = rsvpStatusSubtitle(uiState.myRsvpStatus),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                UpdatePill(onClick = actions::onUpdateRsvp)
            }
        }

        // Stat chips row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            RsvpStatChip(
                modifier = Modifier.weight(1f),
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = Color(0xFF22C55E),
                        modifier = Modifier.size(18.dp),
                    )
                },
                label = stringResource(R.string.event_detail_going_label),
                count = uiState.rsvpYes + uiState.rsvpLate,
            )
            RsvpStatChip(
                modifier = Modifier.weight(1f),
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = null,
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(18.dp),
                    )
                },
                label = stringResource(R.string.event_detail_not_going_label),
                count = uiState.rsvpNo,
            )
        }
    }
}

@Composable
private fun RsvpStatusIcon(status: String?) {
    val (iconVector, tint) = when (status) {
        "yes" -> Icons.Filled.Check to Color(0xFF22C55E)
        "late" -> Icons.Filled.Check to Color(0xFFF97316)
        "no" -> Icons.Filled.Close to Color(0xFFEF4444)
        else -> Icons.Filled.Close to MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
    }

    val borderColor = when (status) {
        "yes" -> Color(0xFF22C55E)
        "late" -> Color(0xFFF97316)
        "no" -> Color(0xFFEF4444)
        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
    }

    Surface(
        shape = CircleShape,
        color = Color.Transparent,
        border = BorderStroke(2.dp, borderColor),
        modifier = Modifier.size(36.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = iconVector,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
private fun UpdatePill(onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(50.dp),
        color = MaterialTheme.colorScheme.primary,
        onClick = onClick,
    ) {
        Text(
            text = stringResource(R.string.event_detail_update_rsvp),
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
        )
    }
}

@Composable
private fun RsvpStatChip(
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit,
    label: String,
    count: Int,
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            icon()
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                )
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

// ── Section: Going ────────────────────────────────────────────────────────────

@Composable
private fun GoingSection(
    uiState: EventDetailScreenUiState,
    actions: EventDetailScreenActions,
) {
    val goingCount = uiState.rsvpYes + uiState.rsvpLate
    val displayed = if (uiState.showAllGoing) uiState.goingMembers
    else uiState.goingMembers.take(3)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionLabelWithBadge(
            text = stringResource(R.string.event_detail_section_going),
            count = goingCount,
            badgeColor = Color(0xFF22C55E),
        )

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column {
                displayed.forEachIndexed { index, member ->
                    if (index > 0) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.background,
                            thickness = 1.dp,
                        )
                    }
                    MemberRow(
                        member = member,
                        trailingIcon = {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF22C55E),
                                modifier = Modifier.size(28.dp),
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp),
                                    )
                                }
                            }
                        },
                    )
                }

                if (uiState.goingMembers.size > 3) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.background,
                        thickness = 1.dp,
                    )
                    TextButton(
                        onClick = actions::onToggleShowAllGoing,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                    ) {
                        Text(
                            text = if (uiState.showAllGoing) {
                                stringResource(R.string.event_detail_show_less)
                            } else {
                                stringResource(R.string.event_detail_view_all, uiState.goingMembers.size)
                            },
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}

// ── Section: Not Going ────────────────────────────────────────────────────────

@Composable
private fun NotGoingSection(
    uiState: EventDetailScreenUiState,
    actions: EventDetailScreenActions,
) {
    val displayed = if (uiState.showAllNotGoing) uiState.notGoingMembers
    else uiState.notGoingMembers.take(3)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionLabelWithBadge(
            text = stringResource(R.string.event_detail_section_not_going),
            count = uiState.rsvpNo,
            badgeColor = Color(0xFFEF4444),
        )

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column {
                displayed.forEachIndexed { index, member ->
                    if (index > 0) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.background,
                            thickness = 1.dp,
                        )
                    }
                    MemberRow(
                        member = member,
                        trailingIcon = {
                            Surface(
                                shape = CircleShape,
                                color = Color.Transparent,
                                border = BorderStroke(2.dp, Color(0xFFEF4444)),
                                modifier = Modifier.size(28.dp),
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = null,
                                        tint = Color(0xFFEF4444),
                                        modifier = Modifier.size(16.dp),
                                    )
                                }
                            }
                        },
                    )
                }

                if (uiState.notGoingMembers.size > 3) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.background,
                        thickness = 1.dp,
                    )
                    TextButton(
                        onClick = actions::onToggleShowAllNotGoing,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                    ) {
                        Text(
                            text = if (uiState.showAllNotGoing) {
                                stringResource(R.string.event_detail_show_less)
                            } else {
                                stringResource(R.string.event_detail_view_all, uiState.notGoingMembers.size)
                            },
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}

// ── Shared sub-composables ────────────────────────────────────────────────────

@Composable
private fun MemberRow(
    member: RsvpMemberUiItem,
    trailingIcon: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        KourtAvatarLeading(
            fallbackText = member.displayName,
            photoUrl = member.avatarUrl,
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = member.displayName,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (member.subtitle.isNotBlank()) {
                Text(
                    text = member.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                )
            }
        }
        trailingIcon()
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f),
    )
}

@Composable
private fun SectionLabelWithBadge(
    text: String,
    count: Int,
    badgeColor: Color,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f),
        )
        Surface(
            shape = RoundedCornerShape(50.dp),
            color = badgeColor,
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            )
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

@Composable
private fun rsvpStatusTitle(status: String?): String = when (status) {
    "yes" -> stringResource(R.string.rsvp_going)
    "late" -> stringResource(R.string.rsvp_gonna_be_late)
    "no" -> stringResource(R.string.rsvp_not_going)
    else -> stringResource(R.string.event_detail_no_response)
}

@Composable
private fun rsvpStatusSubtitle(status: String?): String = when (status) {
    "yes" -> stringResource(R.string.event_detail_status_attending_subtitle)
    "late" -> stringResource(R.string.event_detail_status_late_subtitle)
    "no" -> stringResource(R.string.event_detail_status_not_attending_subtitle)
    else -> stringResource(R.string.event_detail_status_no_response_subtitle)
}

// ── Previews ──────────────────────────────────────────────────────────────────

private val previewEvent = EventDetailUiItem(
    eventId = "evt_1",
    teamId = "team_1",
    title = "Championship Final",
    type = "match",
    status = "upcoming",
    formattedDate = "Wednesday, May 7 • 18:00",
    location = "City Sports Arena",
    notes = "Bring your full kit and arrive 30 minutes early for warm-up.",
)

private val previewGoingMembers = listOf(
    RsvpMemberUiItem("u1", "Marcus Johnson", "Player", "", "yes"),
    RsvpMemberUiItem("u2", "Layla Chen", "Player", "", "late"),
    RsvpMemberUiItem("u3", "Derek Williams", "Player", "", "yes"),
    RsvpMemberUiItem("u4", "Sofia Martinez", "Player", "", "yes"),
)

private val previewNotGoingMembers = listOf(
    RsvpMemberUiItem("u5", "Tom Blake", "Sick", "", "no"),
    RsvpMemberUiItem("u6", "Anna Park", "Out of town", "", "no"),
)

private val previewNominatedPlayers = listOf(
    NominatedPlayerUiItem("u1", "Marcus Johnson", "", 7),
    NominatedPlayerUiItem("u2", "Layla Chen", "", 11),
    NominatedPlayerUiItem("u3", "Derek Williams", "", 23),
    NominatedPlayerUiItem("u4", "Sofia Martinez", "", 0),
)

private val previewUiState = EventDetailScreenUiState(
    isLoading = false,
    event = previewEvent,
    myRsvpStatus = "yes",
    rsvpYes = 3,
    rsvpLate = 1,
    rsvpNo = 2,
    goingMembers = previewGoingMembers,
    notGoingMembers = previewNotGoingMembers,
    isEditable = true,
    canRsvp = true,
    nominatedPlayers = previewNominatedPlayers,
)

private val previewActions = object : EventDetailScreenActions {
    override fun onUpdateRsvp() = Unit
    override fun onRsvpSelected(status: String) = Unit
    override fun onRsvpReasonSelected(reason: String) = Unit
    override fun onRsvpNoteChanged(note: String) = Unit
    override fun onSubmitRsvp() = Unit
    override fun onDismissRsvpSheet() = Unit
    override fun onDismissReasonSheet() = Unit
    override fun onToggleShowAllGoing() = Unit
    override fun onToggleShowAllNotGoing() = Unit
}

@Preview(name = "EventDetail — dark", showBackground = true)
@Composable
private fun EventDetailDarkPreview() {
    KourtTheme(darkTheme = true) {
        EventDetailContent(
            uiState = previewUiState,
            actions = previewActions,
            onBack = {},
            onEdit = {},
        )
    }
}

@Preview(name = "EventDetail — light", showBackground = true)
@Composable
private fun EventDetailLightPreview() {
    KourtTheme(darkTheme = false) {
        EventDetailContent(
            uiState = previewUiState,
            actions = previewActions,
            onBack = {},
            onEdit = {},
        )
    }
}

@Preview(name = "EventDetail — no RSVP, coach view, dark", showBackground = true)
@Composable
private fun EventDetailCoachPreview() {
    KourtTheme(darkTheme = true) {
        EventDetailContent(
            uiState = previewUiState.copy(canRsvp = false, isEditable = true),
            actions = previewActions,
            onBack = {},
            onEdit = {},
        )
    }
}
