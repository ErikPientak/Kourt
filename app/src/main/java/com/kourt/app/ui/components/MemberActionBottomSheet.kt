package com.kourt.app.ui.components

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
import androidx.compose.material3.Surface
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kourt.app.R
import com.kourt.app.ui.theme.KourtTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberActionBottomSheet(
    memberId: String,
    userId: String,
    onDismiss: () -> Unit,
    onViewProfile: (String) -> Unit,
    onAssignToTeam: (String) -> Unit,
    onMakeAdmin: (String) -> Unit,
    onRemoveMember: (String) -> Unit,
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
            userId = userId,
            onDismiss = onDismiss,
            onViewProfile = onViewProfile,
            onAssignToTeam = onAssignToTeam,
            onMakeAdmin = onMakeAdmin,
            onRemoveMember = onRemoveMember,
        )
    }
}

@Composable
private fun MemberActionSheetContent(
    memberId: String,
    userId: String,
    onDismiss: () -> Unit,
    onViewProfile: (String) -> Unit,
    onAssignToTeam: (String) -> Unit,
    onMakeAdmin: (String) -> Unit,
    onRemoveMember: (String) -> Unit,
) {
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

        MemberActionRow(
            icon = painterResource(R.drawable.person_search),
            label = stringResource(R.string.member_action_view_profile),
            contentColor = MaterialTheme.colorScheme.onSurface,
            onClick = { onViewProfile(userId); onDismiss() },
        )

        MemberActionRow(
            icon = painterResource(R.drawable.group),
            label = stringResource(R.string.member_action_assign_team),
            contentColor = MaterialTheme.colorScheme.onSurface,
            onClick = { onAssignToTeam(memberId); onDismiss() },
        )

        MemberActionRow(
            icon = painterResource(R.drawable.admin),
            label = stringResource(R.string.member_action_make_admin),
            contentColor = MaterialTheme.colorScheme.onSurface,
            onClick = { onMakeAdmin(memberId); onDismiss() },
        )

        MemberActionRow(
            icon = painterResource(R.drawable.shield),
            label = stringResource(R.string.member_action_remove),
            contentColor = MaterialTheme.colorScheme.error,
            onClick = { onRemoveMember(memberId); onDismiss() },
        )
    }
}

@Composable
private fun MemberActionRow(
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

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(name = "MemberActionBottomSheet — Dark", showBackground = true, backgroundColor = 0xFF1C1917)
@Composable
private fun MemberActionBottomSheetDarkPreview() {
    KourtTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.surface) {
            MemberActionSheetContent(
                memberId = "preview_member_id",
                userId = "preview_user_id",
                onDismiss = {},
                onViewProfile = {},
                onAssignToTeam = {},
                onMakeAdmin = {},
                onRemoveMember = {},
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
                userId = "preview_user_id",
                onDismiss = {},
                onViewProfile = {},
                onAssignToTeam = {},
                onMakeAdmin = {},
                onRemoveMember = {},
            )
        }
    }
}
