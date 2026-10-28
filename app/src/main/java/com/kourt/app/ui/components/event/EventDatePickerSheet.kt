package com.kourt.app.ui.components.event

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kourt.app.R
import com.kourt.app.ui.components.KourtButton
import com.kourt.app.ui.components.KourtCalendar
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

// ── Date picker ───────────────────────────────────────────────────────────────

fun formatEpochDay(epochDay: Long?, pattern: String, locale: Locale): String =
    epochDay?.let { day ->
        val cal = Calendar.getInstance().apply { timeInMillis = TimeUnit.DAYS.toMillis(day) }
        SimpleDateFormat(pattern, locale).format(cal.time)
    } ?: ""

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDatePickerSheet(
    selectedEpochDay: Long?,
    onDateSelected: (Long) -> Unit,
    onDismiss: () -> Unit,
) {
    val today = remember { Calendar.getInstance() }
    var calYear by rememberSaveable {
        mutableStateOf(
            selectedEpochDay?.let {
                Calendar.getInstance().apply { timeInMillis = TimeUnit.DAYS.toMillis(it) }
                    .get(Calendar.YEAR)
            } ?: today.get(Calendar.YEAR)
        )
    }
    var calMonth by rememberSaveable {
        mutableStateOf(
            selectedEpochDay?.let {
                Calendar.getInstance().apply { timeInMillis = TimeUnit.DAYS.toMillis(it) }
                    .get(Calendar.MONTH)
            } ?: today.get(Calendar.MONTH)
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
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
                selectedEpochDay = selectedEpochDay
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
                onDateSelected = onDateSelected,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(16.dp))
            KourtButton(
                text = stringResource(R.string.dialog_done),
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
            )
        }
    }
}

