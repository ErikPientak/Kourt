package com.kourt.app.ui.components.bottomSheets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kourt.app.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RsvpSelectionBottomSheet(
    onRsvpSelected: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        RsvpSelectionSheetContent(
            onRsvpSelected = onRsvpSelected,
        )
    }
}

@Composable
private fun RsvpSelectionSheetContent(
    onRsvpSelected: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(bottom = 16.dp),
    ) {
        RsvpOptionRow(
            label = stringResource(R.string.rsvp_going),
            iconRes = R.drawable.check,
            tint = Color(0xFF22C55E),
            onClick = { onRsvpSelected("yes") },
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.background)
        RsvpOptionRow(
            label = stringResource(R.string.rsvp_gonna_be_late),
            iconRes = R.drawable.schedule,
            tint = Color(0xFFF97316),
            onClick = { onRsvpSelected("late") },
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.background)
        RsvpOptionRow(
            label = stringResource(R.string.rsvp_not_going),
            iconRes = R.drawable.cancel,
            tint = Color(0xFFEF4444),
            onClick = { onRsvpSelected("no") },
        )
    }
}

@Composable
private fun RsvpOptionRow(
    label: String,
    iconRes: Int,
    tint: Color,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(24.dp),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
