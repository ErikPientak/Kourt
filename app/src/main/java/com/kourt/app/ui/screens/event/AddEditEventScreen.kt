package com.kourt.app.ui.screens.event

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kourt.app.R
import com.kourt.app.navigation.INavigationRouter
import com.kourt.app.ui.components.AuthFieldLabel
import com.kourt.app.ui.components.BaseScreen
import com.kourt.app.ui.components.KourtButton
import com.kourt.app.ui.components.KourtCalendar
import com.kourt.app.ui.components.authTextFieldColors
import com.kourt.app.viewmodel.event.AddEditEventViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable
fun AddEditEventScreen(
    navigation: INavigationRouter,
    viewModel: AddEditEventViewModel = hiltViewModel(),
) {
    val uiState = viewModel.uiState

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            navigation.returnBack()
            viewModel.onSaveConsumed()
        }
    }

    AddEditEventContent(
        uiState = uiState,
        actions = viewModel,
        onCancel = navigation::returnBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditEventContent(
    uiState: AddEditEventScreenUiState,
    actions: AddEditEventScreenActions,
    onCancel: () -> Unit,
) {
    val placeholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
    val iconTintColor = MaterialTheme.colorScheme.onBackground

    val title = if (uiState.isEditMode) {
        stringResource(R.string.edit_event_title)
    } else {
        stringResource(R.string.add_event_title)
    }

    BaseScreen(title = title, onBack = onCancel) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // ── Event Type ──────────────────────────────────────────────────
            SectionLabel(stringResource(R.string.event_type_label))
            SegmentedToggle(
                options = listOf(
                    stringResource(R.string.event_type_practice),
                    stringResource(R.string.event_type_match),
                ),
                selectedIndex = if (uiState.eventType == "match") 1 else 0,
                onSelect = { idx -> actions.onEventTypeChange(if (idx == 1) "match" else "practice") },
                modifier = Modifier.fillMaxWidth(),
            )

            // ── Session Name ─────────────────────────────────────────────
            val sessionPlaceholder = if (uiState.eventType == "match") {
                stringResource(R.string.event_session_name_placeholder_match)
            } else {
                stringResource(R.string.event_session_name_placeholder_practice)
            }
            EventTextField(
                label = stringResource(R.string.event_session_name_label),
                value = uiState.sessionName,
                onValueChange = actions::onSessionNameChange,
                placeholder = sessionPlaceholder,
                placeholderColor = placeholderColor,
                iconTintColor = iconTintColor,
            )

            // ── Match-only fields ─────────────────────────────────────────
            if (uiState.eventType == "match") {
                EventTextField(
                    label = stringResource(R.string.event_opponent_label),
                    value = uiState.opponent,
                    onValueChange = actions::onOpponentChange,
                    placeholder = stringResource(R.string.event_opponent_placeholder),
                    placeholderColor = placeholderColor,
                    iconTintColor = iconTintColor,
                )

                SectionLabel(stringResource(R.string.event_venue_type_label))
                SegmentedToggle(
                    options = listOf(
                        stringResource(R.string.event_venue_home),
                        stringResource(R.string.event_venue_away),
                    ),
                    selectedIndex = if (uiState.venueType == "away") 1 else 0,
                    onSelect = { idx -> actions.onVenueTypeChange(if (idx == 1) "away" else "home") },
                    modifier = Modifier.fillMaxWidth(),
                )

                NominationRow(onClick = actions::onNominationClick)
            }

            // ── Date ─────────────────────────────────────────────────────
            val locale = Locale.getDefault()
            val datePattern = remember(locale) {
                android.text.format.DateFormat.getBestDateTimePattern(locale, "MMMd yyyy")
            }
            val dateLabel = remember(uiState.selectedEpochDay, datePattern, locale) {
                uiState.selectedEpochDay?.let { day ->
                    val cal = Calendar.getInstance().apply {
                        timeInMillis = TimeUnit.DAYS.toMillis(day)
                    }
                    SimpleDateFormat(datePattern, locale).format(cal.time)
                } ?: ""
            }
            ReadOnlyField(
                label = stringResource(R.string.event_date_label),
                value = dateLabel,
                placeholder = stringResource(R.string.event_date_label),
                trailingIconRes = R.drawable.event,
                onClick = actions::onDatePickerOpen,
                placeholderColor = placeholderColor,
                iconTintColor = iconTintColor,
            )

            // ── Start / End Time ─────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                ReadOnlyField(
                    label = stringResource(R.string.event_start_time_label),
                    value = uiState.startTime,
                    placeholder = "00:00",
                    trailingIconRes = R.drawable.schedule,
                    onClick = actions::onStartTimePickerOpen,
                    placeholderColor = placeholderColor,
                    iconTintColor = iconTintColor,
                    modifier = Modifier.weight(1f),
                )
                ReadOnlyField(
                    label = stringResource(R.string.event_end_time_label),
                    value = uiState.endTime,
                    placeholder = "00:00",
                    trailingIconRes = R.drawable.schedule,
                    onClick = actions::onEndTimePickerOpen,
                    placeholderColor = placeholderColor,
                    iconTintColor = iconTintColor,
                    modifier = Modifier.weight(1f),
                )
            }

            // ── Repeat weekly (Practice only) ────────────────────────────
            if (uiState.eventType == "practice") {
                RepeatRow(
                    isEnabled = uiState.isRepeatEnabled,
                    repeatDays = uiState.repeatDays,
                    onRowClick = {
                        if (!uiState.isRepeatEnabled) actions.onRepeatSheetOpen()
                    },
                    onToggle = { enabled ->
                        if (enabled) actions.onRepeatSheetOpen() else actions.onRepeatToggle(false)
                    },
                    iconTintColor = iconTintColor,
                )
            }

            // ── Location ─────────────────────────────────────────────────
            EventTextField(
                label = stringResource(R.string.event_location_label),
                value = uiState.location,
                onValueChange = actions::onLocationChange,
                placeholder = stringResource(R.string.event_location_placeholder),
                placeholderColor = placeholderColor,
                iconTintColor = iconTintColor,
                leadingIconRes = R.drawable.city,
            )

            // ── Coach's Notes ─────────────────────────────────────────────
            val notesPlaceholder = if (uiState.eventType == "match") {
                stringResource(R.string.event_notes_placeholder_match)
            } else {
                stringResource(R.string.event_notes_placeholder_practice)
            }
            EventTextField(
                label = stringResource(R.string.event_notes_label),
                value = uiState.notes,
                onValueChange = actions::onNotesChange,
                placeholder = notesPlaceholder,
                placeholderColor = placeholderColor,
                iconTintColor = iconTintColor,
                minLines = 3,
            )

            // ── Error ────────────────────────────────────────────────────
            if (uiState.error != null) {
                Text(
                    text = stringResource(uiState.error),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            // ── CTA ──────────────────────────────────────────────────────
            KourtButton(
                text = if (uiState.isEditMode) {
                    stringResource(R.string.event_save_button)
                } else {
                    stringResource(R.string.event_create_button)
                },
                onClick = actions::onSave,
                enabled = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth(),
            )

            if (uiState.eventType == "practice") {
                TextButton(
                    onClick = onCancel,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = stringResource(R.string.event_cancel),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }

    // ── Date picker bottom sheet ──────────────────────────────────────────────
    if (uiState.isDatePickerOpen) {
        val today = remember { Calendar.getInstance() }
        var calYear by rememberSaveable {
            mutableStateOf(
                uiState.selectedEpochDay?.let {
                    Calendar.getInstance().apply { timeInMillis = TimeUnit.DAYS.toMillis(it) }
                        .get(Calendar.YEAR)
                } ?: today.get(Calendar.YEAR)
            )
        }
        var calMonth by rememberSaveable {
            mutableStateOf(
                uiState.selectedEpochDay?.let {
                    Calendar.getInstance().apply { timeInMillis = TimeUnit.DAYS.toMillis(it) }
                        .get(Calendar.MONTH)
                } ?: today.get(Calendar.MONTH)
            )
        }

        ModalBottomSheet(
            onDismissRequest = actions::onDatePickerDismiss,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.background,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .padding(bottom = 24.dp),
            ) {
                KourtCalendar(
                    year = calYear,
                    month = calMonth,
                    selectedEpochDay = uiState.selectedEpochDay
                        ?: TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis()),
                    eventDays = emptySet(),
                    onPreviousMonth = {
                        val cal = Calendar.getInstance().apply {
                            set(calYear, calMonth, 1); add(Calendar.MONTH, -1)
                        }
                        calYear = cal.get(Calendar.YEAR)
                        calMonth = cal.get(Calendar.MONTH)
                    },
                    onNextMonth = {
                        val cal = Calendar.getInstance().apply {
                            set(calYear, calMonth, 1); add(Calendar.MONTH, 1)
                        }
                        calYear = cal.get(Calendar.YEAR)
                        calMonth = cal.get(Calendar.MONTH)
                    },
                    onDateSelected = actions::onDateSelected,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(16.dp))
                KourtButton(
                    text = stringResource(R.string.dialog_done),
                    onClick = actions::onDatePickerDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                )
            }
        }
    }

    // ── Start time picker ────────────────────────────────────────────────────
    if (uiState.isStartTimePickerOpen) {
        TimePickerDialog(
            onDismiss = actions::onStartTimePickerDismiss,
            onConfirm = { h, m -> actions.onStartTimeSelected("%02d:%02d".format(h, m)) },
        )
    }

    // ── End time picker ──────────────────────────────────────────────────────
    if (uiState.isEndTimePickerOpen) {
        TimePickerDialog(
            onDismiss = actions::onEndTimePickerDismiss,
            onConfirm = { h, m -> actions.onEndTimeSelected("%02d:%02d".format(h, m)) },
        )
    }

    // ── Repeat on bottom sheet ───────────────────────────────────────────────
    if (uiState.isRepeatSheetOpen) {
        RepeatOnBottomSheet(
            initialDays = uiState.repeatDays,
            onDismiss = actions::onRepeatSheetDismiss,
            onDone = actions::onRepeatDaysChanged,
        )
    }
}

// ── Shared field composables ──────────────────────────────────────────────────

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
    )
}

@Composable
private fun EventTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    placeholderColor: Color,
    iconTintColor: Color,
    modifier: Modifier = Modifier,
    leadingIconRes: Int? = null,
    minLines: Int = 1,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        AuthFieldLabel(label = label)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyMedium,
                    color = placeholderColor,
                )
            },
            leadingIcon = leadingIconRes?.let {
                {
                    Icon(
                        painter = painterResource(it),
                        contentDescription = null,
                        tint = iconTintColor,
                        modifier = Modifier.size(18.dp),
                    )
                }
            },
            minLines = minLines,
            shape = RoundedCornerShape(12.dp),
            colors = authTextFieldColors(placeholderColor = placeholderColor),
        )
    }
}

@Composable
private fun ReadOnlyField(
    label: String,
    value: String,
    placeholder: String,
    trailingIconRes: Int,
    onClick: () -> Unit,
    placeholderColor: Color,
    iconTintColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        AuthFieldLabel(label = label)
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            enabled = false,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
            placeholder = {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyMedium,
                    color = placeholderColor,
                )
            },
            trailingIcon = {
                Icon(
                    painter = painterResource(trailingIconRes),
                    contentDescription = null,
                    tint = iconTintColor,
                    modifier = Modifier.size(18.dp),
                )
            },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                disabledContainerColor = MaterialTheme.colorScheme.surface,
                disabledBorderColor = Color.Transparent,
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledPlaceholderColor = placeholderColor,
                disabledTrailingIconColor = iconTintColor,
            ),
        )
    }
}

// ── Segmented toggle ──────────────────────────────────────────────────────────

@Composable
private fun SegmentedToggle(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.height(52.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
        ) {
            options.forEachIndexed { idx, label ->
                val isSelected = idx == selectedIndex
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary
                            else Color.Transparent
                        )
                        .clickable { onSelect(idx) },
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        ),
                        color = when {
                            isSelected -> MaterialTheme.colorScheme.onPrimary
                            else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        },
                    )
                }
            }
        }
    }
}

// ── Nomination row (Match only) ───────────────────────────────────────────────

@Composable
private fun NominationRow(onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                painter = painterResource(R.drawable.group),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
            Text(
                text = stringResource(R.string.event_nomination_row),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

// ── Repeat weekly row ─────────────────────────────────────────────────────────

@Composable
private fun RepeatRow(
    isEnabled: Boolean,
    repeatDays: Set<Int>,
    onRowClick: () -> Unit,
    onToggle: (Boolean) -> Unit,
    iconTintColor: Color
) {
    val locale = Locale.getDefault()
    val subtitle = remember(repeatDays, locale) {
        if (repeatDays.isEmpty()) return@remember ""
        val ordered = (Calendar.MONDAY..Calendar.SATURDAY).toList() + listOf(Calendar.SUNDAY)
        repeatDays
            .sortedBy { ordered.indexOf(it) }
            .joinToString(" , ") { dayConst ->
                Calendar.getInstance().apply { set(Calendar.DAY_OF_WEEK, dayConst) }
                    .getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, locale)
                    ?: ""
            }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onRowClick),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                painter = painterResource(R.drawable.event_repeat),
                contentDescription = null,
                tint = iconTintColor,
                modifier = Modifier.size(20.dp),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.event_repeat_weekly_label),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (subtitle.isNotEmpty()) {
                    Text(
                        text = stringResource(R.string.event_repeat_summary, subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    )
                }
            }
            Switch(
                checked = isEnabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                ),
            )
        }
    }
}

// ── Time picker dialog ────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: (hour: Int, minute: Int) -> Unit,
) {
    val state = rememberTimePickerState(is24Hour = true)
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onConfirm(state.hour, state.minute) }) {
                Text(stringResource(android.R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(android.R.string.cancel))
            }
        },
        text = {
            TimePicker(
                state = state,
                colors = TimePickerDefaults.colors(
                    clockDialColor = MaterialTheme.colorScheme.surface,
                    selectorColor = MaterialTheme.colorScheme.primary,
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    )
}

// ── Repeat on bottom sheet ────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RepeatOnBottomSheet(
    initialDays: Set<Int>,
    onDismiss: () -> Unit,
    onDone: (Set<Int>) -> Unit,
) {
    val locale = Locale.getDefault()
    var tempDays by rememberSaveable { mutableStateOf(initialDays) }

    // Locale-aware day order starting from firstDayOfWeek
    val dayConstants = remember(locale) {
        val firstDay = Calendar.getInstance(locale).firstDayOfWeek
        (0 until 7).map { offset -> ((firstDay - 1 + offset) % 7) + 1 }
    }
    val dayLabels = remember(dayConstants, locale) {
        dayConstants.map { dayConst ->
            Calendar.getInstance().apply { set(Calendar.DAY_OF_WEEK, dayConst) }
                .getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, locale)
                ?.uppercase(locale)
                ?.take(2)
                ?: ""
        }
    }

    val summary = remember(tempDays, locale) {
        if (tempDays.isEmpty()) return@remember ""
        val ordered = (Calendar.MONDAY..Calendar.SATURDAY).toList() + listOf(Calendar.SUNDAY)
        tempDays
            .sortedBy { ordered.indexOf(it) }
            .joinToString(" , ") { dayConst ->
                Calendar.getInstance().apply { set(Calendar.DAY_OF_WEEK, dayConst) }
                    .getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, locale)
                    ?: ""
            }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.event_repeat_on_title),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                TextButton(onClick = onDismiss) {
                    Text(
                        text = "×",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            // Day pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                dayConstants.forEachIndexed { idx, dayConst ->
                    val isSelected = dayConst in tempDays
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surface
                            )
                            .clickable {
                                tempDays = if (isSelected) tempDays - dayConst else tempDays + dayConst
                            },
                    ) {
                        Text(
                            text = dayLabels[idx],
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        )
                    }
                }
            }

            // Summary text
            if (summary.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.event_repeat_occurs_summary, summary),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
            }

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text = stringResource(R.string.action_cancel),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    )
                }
                KourtButton(
                    text = stringResource(R.string.dialog_done),
                    onClick = { onDone(tempDays) },
                    modifier = Modifier.weight(2f),
                )
            }
        }
    }
}
