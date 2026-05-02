package com.kourt.app.ui.components.bottomSheets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kourt.app.R
import com.kourt.app.ui.components.KourtButton
import com.kourt.app.ui.components.ReasonCard

private data class RoleOption(
    val key: String,
    val labelRes: Int,
    val iconRes: Int,
)

private val ROLE_OPTIONS = listOf(
    RoleOption("coach", R.string.members_filter_coach, R.drawable.whistle),
    RoleOption("assistant", R.string.members_filter_assistant, R.drawable.assistant),
    RoleOption("player", R.string.members_filter_player, R.drawable.basketball),
    RoleOption("parent", R.string.members_filter_parent, R.drawable.group),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangeRoleBottomSheet(
    selectedRole: String?,
    selectedMember: String,
    onRoleSelected: (String) -> Unit,
    onUpdateRole: () -> Unit,
    onDismiss: () -> Unit,

){
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        RoleSheetContent(
            selectedMember = selectedMember,
            selectedRole = selectedRole,
            onRoleSelected = onRoleSelected,
            onUpdateRole = { onUpdateRole(); onDismiss() },
        )
    }
}

@Composable
private fun RoleSheetContent(
    selectedMember: String,
    selectedRole: String?,
    onRoleSelected: (String) -> Unit,
    onUpdateRole: () -> Unit,
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
            text = stringResource(R.string.role_sheet_title),
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
        )

        // 2×2 reason grid
        val rows = ROLE_OPTIONS.chunked(2)
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
                            isSelected = selectedRole == option.key,
                            onClick = { onRoleSelected(option.key) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }

        KourtButton(
            text = stringResource(R.string.role_update),
            onClick = onUpdateRole,
            enabled = selectedRole != null,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}