package com.kourt.app.ui.screens.event.nomination

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import com.kourt.app.viewmodel.event.NominationViewModel

@Composable
fun NominationScreen(
    navigation: INavigationRouter,
    viewModel: NominationViewModel = hiltViewModel(),
) {
    val uiState = viewModel.uiState

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            navigation.returnBack()
            viewModel.onSaveConsumed()
        }
    }

    NominationScreenContent(
        uiState = uiState,
        onBack = navigation::returnBack,
        onTogglePlayer = viewModel::onTogglePlayer,
        onNominate = viewModel::onSave,
    )
}

@Composable
private fun NominationScreenContent(
    uiState: NominationScreenUiState,
    onBack: () -> Unit,
    onTogglePlayer: (String) -> Unit,
    onNominate: () -> Unit,
) {
    BaseScreen(
        title = stringResource(R.string.nomination_title),
        onBack = onBack,
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    NominationHeaderCard(
                        selectedCount = uiState.selectedCount,
                        rosterLimit = uiState.rosterLimit,
                    )

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
                    ) {
                        item { Spacer(modifier = Modifier.height(8.dp)) }
                        items(uiState.players, key = { it.userId }) { player ->
                            NominationPlayerRow(
                                player = player,
                                onToggle = { onTogglePlayer(player.userId) },
                            )
                        }
                        item { Spacer(modifier = Modifier.height(8.dp)) }
                    }

                    KourtButton(
                        text = stringResource(R.string.nomination_button),
                        onClick = onNominate,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun NominationHeaderCard(
    selectedCount: Int,
    rosterLimit: Int,
) {
    val primary = MaterialTheme.colorScheme.primary

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.nomination_roster_limit),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = primary,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = stringResource(R.string.nomination_selected, selectedCount, rosterLimit),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = primary,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            val progress = if (rosterLimit > 0) selectedCount.toFloat() / rosterLimit.toFloat() else 0f

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = primary,
                trackColor = primary.copy(alpha = 0.15f),
            )
        }
    }
}

@Composable
private fun NominationPlayerRow(
    player: NominationPlayerItem,
    onToggle: () -> Unit,
) {
    val attendancePct = (player.attendanceRate * 100).toInt()

    Surface(
        shape = RoundedCornerShape(52.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() },
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            KourtAvatarLeading(
                fallbackText = player.displayName,
                size = 44.dp,
                photoUrl = player.avatarUrl,
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = player.displayName,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(R.string.nomination_attendance_rate, attendancePct),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            SelectionCircle(isSelected = player.isSelected)
        }
    }
}

@Composable
private fun SelectionCircle(isSelected: Boolean) {
    val primary = MaterialTheme.colorScheme.primary
    val onSurface = MaterialTheme.colorScheme.onSurface

    if (isSelected) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(color = primary, shape = CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp),
            )
        }
    } else {
        Box(
            modifier = Modifier
                .size(28.dp)
                .border(width = 2.dp, color = onSurface.copy(alpha = 0.3f), shape = CircleShape),
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1C1917)
@Composable
private fun NominationScreenContentPreview() {
    val players = listOf(
        NominationPlayerItem(
            userId = "1",
            displayName = "Marcus Johnson",
            avatarUrl = "",
            attendanceRate = 0.87f,
            isSelected = true,
        ),
        NominationPlayerItem(
            userId = "2",
            displayName = "Tyler Brooks",
            avatarUrl = "",
            attendanceRate = 0.65f,
            isSelected = true,
        ),
        NominationPlayerItem(
            userId = "3",
            displayName = "Devon Carter",
            avatarUrl = "",
            attendanceRate = 0.50f,
            isSelected = false,
        ),
        NominationPlayerItem(
            userId = "4",
            displayName = "Jaylen Williams",
            avatarUrl = "",
            attendanceRate = 0.92f,
            isSelected = false,
        ),
    )

    NominationScreenContent(
        uiState = NominationScreenUiState(
            isLoading = false,
            players = players,
            rosterLimit = 12,
        ),
        onBack = {},
        onTogglePlayer = {},
        onNominate = {},
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF1C1917)
@Composable
private fun NominationScreenLoadingPreview() {
    NominationScreenContent(
        uiState = NominationScreenUiState(isLoading = true),
        onBack = {},
        onTogglePlayer = {},
        onNominate = {},
    )
}
