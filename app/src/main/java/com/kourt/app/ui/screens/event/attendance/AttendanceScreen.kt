package com.kourt.app.ui.screens.event.attendance

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kourt.app.R
import com.kourt.app.navigation.INavigationRouter
import com.kourt.app.ui.components.BaseScreen
import com.kourt.app.ui.components.KourtAvatarLeading
import com.kourt.app.ui.components.KourtButton
import com.kourt.app.ui.theme.KourtTheme
import com.kourt.app.viewmodel.event.AttendanceViewModel

// ── Status pill colours ───────────────────────────────────────────────────────

private val StatusOnTimeColor   = Color(0xFF22C55E)
private val StatusExcusedColor  = Color(0xFF6B7280)
private val StatusUnexcusedColor = Color(0xFFEF4444)

// Label → Firestore value
private val statusPills = listOf(
    "On Time"   to "on_time",
    "Late"      to "late",
    "Excused"   to "excused",
    "Unexcused" to "unexcused",
)

// ── Entry point ───────────────────────────────────────────────────────────────

@Composable
fun AttendanceScreen(
    navigation: INavigationRouter,
    viewModel: AttendanceViewModel = hiltViewModel(),
) {
    val uiState = viewModel.uiState

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            navigation.returnBack()
            viewModel.onSavedConsumed()
        }
    }

    AttendanceScreenContent(
        uiState = uiState,
        onBack = navigation::returnBack,
        onStatusSelected = viewModel::onStatusSelected,
        onFilterChanged = viewModel::onFilterChanged,
        onSave = viewModel::onSave,
    )
}

// ── Content composable ────────────────────────────────────────────────────────

@Composable
internal fun AttendanceScreenContent(
    uiState: AttendanceScreenUiState,
    onBack: () -> Unit,
    onStatusSelected: (userId: String, status: String) -> Unit,
    onFilterChanged: (AttendanceFilter) -> Unit,
    onSave: () -> Unit,
) {
    val title = if (uiState.eventTitle.isNotBlank() && uiState.eventDate.isNotBlank()) {
        "${uiState.eventTitle} - ${uiState.eventDate}"
    } else {
        uiState.eventTitle.ifBlank { stringResource(R.string.attendance_screen_title) }
    }

    BaseScreen(
        title = title,
        onBack = onBack,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                // Filter chip row
                AttendanceFilterRow(
                    selected = uiState.filter,
                    onFilterChanged = onFilterChanged,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )

                val filteredMembers = when (uiState.filter) {
                    AttendanceFilter.ALL -> uiState.members
                    AttendanceFilter.EXCUSED -> uiState.members.filter { it.status == "excused" }
                    AttendanceFilter.UNEXCUSED -> uiState.members.filter { it.status == "unexcused" }
                }

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(filteredMembers, key = { it.userId }) { member ->
                        AttendanceMemberCard(
                            member = member,
                            onStatusSelected = onStatusSelected,
                        )
                    }
                }
            }

            // Save button — outside scroll area
            KourtButton(
                text = stringResource(R.string.attendance_save_button),
                onClick = onSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                enabled = !uiState.isSaving,
            )
        }
    }
}

// ── Filter chip row ───────────────────────────────────────────────────────────

@Composable
private fun AttendanceFilterRow(
    selected: AttendanceFilter,
    onFilterChanged: (AttendanceFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    val chips = listOf(
        stringResource(R.string.attendance_filter_all)      to AttendanceFilter.ALL,
        stringResource(R.string.attendance_filter_excused)  to AttendanceFilter.EXCUSED,
        stringResource(R.string.attendance_filter_unexcused) to AttendanceFilter.UNEXCUSED,
    )

    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(chips) { (label, filter) ->
            val isSelected = selected == filter
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surface,
                    )
                    .clickable { onFilterChanged(filter) }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = if (isSelected) Color.White
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                )
            }
        }
    }
}

// ── Member card ───────────────────────────────────────────────────────────────

@Composable
private fun AttendanceMemberCard(
    member: AttendanceMemberUiItem,
    onStatusSelected: (userId: String, status: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Avatar + name row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                KourtAvatarLeading(
                    fallbackText = member.displayName,
                    photoUrl = member.avatarUrl,
                    size = 56.dp,
                )
                Text(
                    text = member.displayName,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            // Status pill row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                statusPills.forEach { (label, value) ->
                    AttendanceStatusPill(
                        label = label,
                        value = value,
                        isSelected = member.status == value,
                        selectedColor = statusPillColor(value),
                        modifier = Modifier.weight(1f),
                        onClick = { onStatusSelected(member.userId, value) },
                    )
                }
            }
        }
    }
}

@Composable
private fun AttendanceStatusPill(
    label: String,
    value: String,
    isSelected: Boolean,
    selectedColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(RoundedCornerShape(50.dp))
            .background(if (isSelected) selectedColor else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = if (isSelected) Color.White
            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
        )
    }
}

@Composable
private fun statusPillColor(value: String): Color = when (value) {
    "on_time"   -> StatusOnTimeColor
    "late"      -> MaterialTheme.colorScheme.primary
    "excused"   -> StatusExcusedColor
    "unexcused" -> StatusUnexcusedColor
    else        -> Color.Transparent
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(name = "AttendanceScreen — dark", showBackground = true)
@Composable
private fun AttendanceScreenDarkPreview() {
    KourtTheme(darkTheme = true) {
        AttendanceScreenContent(
            uiState = AttendanceScreenUiState(
                eventTitle = "Shooting Drills",
                eventDate = "Oct 17",
                members = listOf(
                    AttendanceMemberUiItem("1", "Marcus Johnson", "", "on_time"),
                    AttendanceMemberUiItem("2", "Tyler Brooks", "", null),
                    AttendanceMemberUiItem("3", "Jamie Lee", "", "late"),
                    AttendanceMemberUiItem("4", "Sam Torres", "", "excused"),
                ),
                filter = AttendanceFilter.ALL,
            ),
            onBack = {},
            onStatusSelected = { _, _ -> },
            onFilterChanged = {},
            onSave = {},
        )
    }
}

@Preview(name = "AttendanceScreen — light", showBackground = true)
@Composable
private fun AttendanceScreenLightPreview() {
    KourtTheme(darkTheme = false) {
        AttendanceScreenContent(
            uiState = AttendanceScreenUiState(
                eventTitle = "Shooting Drills",
                eventDate = "Oct 17",
                members = listOf(
                    AttendanceMemberUiItem("1", "Marcus Johnson", "", "on_time"),
                    AttendanceMemberUiItem("2", "Tyler Brooks", "", null),
                ),
                filter = AttendanceFilter.ALL,
            ),
            onBack = {},
            onStatusSelected = { _, _ -> },
            onFilterChanged = {},
            onSave = {},
        )
    }
}
