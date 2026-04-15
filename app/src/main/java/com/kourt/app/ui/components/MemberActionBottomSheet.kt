package com.kourt.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kourt.app.R
import com.kourt.app.ui.theme.KourtTheme

/**
 * Modal bottom sheet that appears when the three-dot menu on a [MemberCard] is tapped.
 *
 * All action callbacks include the [memberId] so the ViewModel can act on the correct
 * TeamMember document without any local state in this composable.
 *
 * @param memberId  The TeamMember document ID of the member being acted on.
 * @param onDismiss Called when the sheet is dragged down or an action is tapped.
 *                  The ViewModel sets `selectedMemberId = null` in response.
 * @param onEditMember        "Edit Member" row tapped.
 * @param onChangeRole        "Change Role" row tapped.
 * @param onRemoveFromClub    "Remove from Club" row tapped (destructive).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberActionBottomSheet(
    memberId: String,
    onDismiss: () -> Unit,
    onEditMember: (String) -> Unit,
    onChangeRole: (String) -> Unit,
    onRemoveFromClub: (String) -> Unit,
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
        MemberActionSheetContent(
            memberId = memberId,
            onDismiss = onDismiss,
            onEditMember = onEditMember,
            onChangeRole = onChangeRole,
            onRemoveFromClub = onRemoveFromClub,
        )
    }
}

@Composable
private fun MemberActionSheetContent(
    memberId: String,
    onDismiss: () -> Unit,
    onEditMember: (String) -> Unit,
    onChangeRole: (String) -> Unit,
    onRemoveFromClub: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(bottom = 8.dp),
    ) {
        // Title
        Text(
            text = stringResource(R.string.member_action_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
        )

        // Edit Member
        MemberActionRow(
            icon = Icons.Default.Edit,
            label = stringResource(R.string.member_action_edit),
            contentColor = MaterialTheme.colorScheme.onSurface,
            onClick = {
                onEditMember(memberId)
                onDismiss()
            },
        )

        // Change Role
        MemberActionRow(
            icon = Icons.Default.Person,
            label = stringResource(R.string.member_action_change_role),
            contentColor = MaterialTheme.colorScheme.onSurface,
            onClick = {
                onChangeRole(memberId)
                onDismiss()
            },
        )

        // Remove from Club — destructive
        MemberActionRow(
            icon = Icons.Default.Delete,
            label = stringResource(R.string.member_action_remove),
            contentColor = MaterialTheme.colorScheme.error,
            onClick = {
                onRemoveFromClub(memberId)
                onDismiss()
            },
        )
    }
}

/**
 * A single full-width action row: leading icon + label text, themed with [contentColor].
 * Uses [TextButton] for correct ripple and minimum touch target (48dp height).
 */
@Composable
private fun MemberActionRow(
    icon: ImageVector,
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
                imageVector = icon,
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

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(name = "MemberActionBottomSheet — Dark", showBackground = true, backgroundColor = 0xFF1C1917)
@Composable
private fun MemberActionBottomSheetDarkPreview() {
    KourtTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.surface) {
            MemberActionSheetContent(
                memberId = "preview_member_id",
                onDismiss = {},
                onEditMember = {},
                onChangeRole = {},
                onRemoveFromClub = {},
            )
        }
    }
}

@Preview(name = "MemberActionBottomSheet — Light", showBackground = true, backgroundColor = 0xFFEEF0F8)
@Composable
private fun MemberActionBottomSheetLightPreview() {
    KourtTheme(darkTheme = false) {
        Surface(color = MaterialTheme.colorScheme.surface) {
            MemberActionSheetContent(
                memberId = "preview_member_id",
                onDismiss = {},
                onEditMember = {},
                onChangeRole = {},
                onRemoveFromClub = {},
            )
        }
    }
}
