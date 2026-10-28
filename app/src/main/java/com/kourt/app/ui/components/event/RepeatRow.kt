package com.kourt.app.ui.components.event

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kourt.app.R
import java.util.Calendar

// repeatDays are ISO days (1 = Mon … 7 = Sun, matching java.time); Calendar uses 1 = Sun … 7 = Sat.
internal fun isoToCalendarDay(isoDay: Int): Int = if (isoDay == 7) Calendar.SUNDAY else isoDay + 1
internal fun calendarToIsoDay(calendarDay: Int): Int = if (calendarDay == Calendar.SUNDAY) 7 else calendarDay - 1

@Composable
fun RepeatRow(
    isEnabled: Boolean,
    repeatDays: Set<Int>,
    onRowClick: () -> Unit,
    onToggle: (Boolean) -> Unit,
    iconTintColor: Color
) {
    val locale = LocalLocale.current.platformLocale
    val subtitle = remember(repeatDays, locale) {
        if (repeatDays.isEmpty()) return@remember ""
        repeatDays
            .sorted()
            .joinToString(" , ") { isoDay ->
                Calendar.getInstance().apply { set(Calendar.DAY_OF_WEEK, isoToCalendarDay(isoDay)) }
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