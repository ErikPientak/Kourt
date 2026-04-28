package com.kourt.app.ui.components.bottomSheets

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kourt.app.R
import com.kourt.app.ui.components.KourtButton

private data class ReasonOption(
    val key: String,
    val labelRes: Int,
    val iconRes: Int,
)

private val REASON_OPTIONS = listOf(
    ReasonOption("sick", R.string.rsvp_reason_sick, R.drawable.sick),
    ReasonOption("work", R.string.rsvp_reason_work, R.drawable.work),
    ReasonOption("out_of_town", R.string.rsvp_reason_out_of_town, R.drawable.flight),
    ReasonOption("other", R.string.rsvp_reason_other, R.drawable.more_vert),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RsvpReasonBottomSheet(
    selectedReason: String?,
    reasonNote: String,
    isSubmitting: Boolean,
    onReasonSelected: (String) -> Unit,
    onNoteChanged: (String) -> Unit,
    onSubmitReason: () -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        RsvpReasonSheetContent(
            selectedReason = selectedReason,
            reasonNote = reasonNote,
            isSubmitting = isSubmitting,
            onReasonSelected = onReasonSelected,
            onNoteChanged = onNoteChanged,
            onSubmitReason = { onSubmitReason(); onDismiss() },
        )
    }
}

@Composable
private fun RsvpReasonSheetContent(
    selectedReason: String?,
    reasonNote: String,
    isSubmitting: Boolean,
    onReasonSelected: (String) -> Unit,
    onNoteChanged: (String) -> Unit,
    onSubmitReason: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp)
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(
            text = stringResource(R.string.rsvp_reason_title),
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
        )

        // 2×2 reason grid
        val rows = REASON_OPTIONS.chunked(2)
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            rows.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    row.forEach { option ->
                        ReasonCard(
                            label = stringResource(option.labelRes),
                            iconRes = option.iconRes,
                            isSelected = selectedReason == option.key,
                            onClick = { onReasonSelected(option.key) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }

        OutlinedTextField(
            value = reasonNote,
            onValueChange = onNoteChanged,
            placeholder = {
                Text(
                    text = stringResource(R.string.rsvp_reason_note_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp)),
            minLines = 3,
            maxLines = 5,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedContainerColor = MaterialTheme.colorScheme.background,
                focusedContainerColor = MaterialTheme.colorScheme.background,
            ),
        )

        KourtButton(
            text = stringResource(R.string.rsvp_submit_reason),
            onClick = onSubmitReason,
            enabled = selectedReason != null && !isSubmitting,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun ReasonCard(
    label: String,
    iconRes: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val primary = MaterialTheme.colorScheme.primary
    val shape = RoundedCornerShape(12.dp)

    Column(
        modifier = modifier
            .aspectRatio(1f)
            .clip(shape)
            .background(
                if (isSelected) primary.copy(alpha = 0.12f)
                else MaterialTheme.colorScheme.background,
            )
            .then(
                if (isSelected) Modifier.border(BorderStroke(2.dp, primary), shape)
                else Modifier.border(BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)), shape)
            )
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = if (isSelected) primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.size(32.dp),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
            color = if (isSelected) primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}
