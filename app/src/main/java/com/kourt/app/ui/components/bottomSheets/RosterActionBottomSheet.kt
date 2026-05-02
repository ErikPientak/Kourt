package com.kourt.app.ui.components.bottomSheets

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kourt.app.R


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RosterActionBottomSheet(
    memberId: String,
    onDismiss: () -> Unit,
    onRemoveMember: (String) -> Unit,
    onChangeRole: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        RosterActionSheetContent(
            memberId = memberId,
            onDismiss = onDismiss,
            onRemoveMember = onRemoveMember,
            onChangeRole = onChangeRole,
        )
    }
}

@Composable
fun RosterActionSheetContent(
    memberId: String,
    onDismiss: () -> Unit,
    onRemoveMember: (String) -> Unit,
    onChangeRole: () -> Unit
    ){
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(bottom = 8.dp),
    ) {
        Text(
            text = stringResource(R.string.member_action_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
        )

        RosterActionRow(
            icon = painterResource(R.drawable.edit),
            label = stringResource(R.string.member_action_assign_role),
            contentColor = MaterialTheme.colorScheme.onSurface,
            onClick = { onChangeRole(); onDismiss() },
        )

        RosterActionRow(
            icon = painterResource(R.drawable.shield),
            label = stringResource(R.string.member_action_remove),
            contentColor = MaterialTheme.colorScheme.error,
            onClick = { onRemoveMember(memberId) },
        )
    }
}

@Composable
private fun RosterActionRow(
    icon: Painter,
    label: String,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TextButton(
        onClick = onClick,
        modifier = modifier
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