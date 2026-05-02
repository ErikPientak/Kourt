package com.kourt.app.ui.screens.event.matchstats

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kourt.app.R
import com.kourt.app.navigation.INavigationRouter
import com.kourt.app.ui.components.BaseScreen
import com.kourt.app.ui.components.KourtAvatarLeading
import com.kourt.app.ui.components.KourtButton
import com.kourt.app.ui.theme.KourtTheme
import com.kourt.app.viewmodel.event.MatchStatsViewModel

// ── Constants ─────────────────────────────────────────────────────────────────

private val AvatarColumnWidth = 68.dp
private val StatColumnWidth   = 72.dp

private data class StatColumnDef(val label: String, val field: StatField)

private val statColumns = listOf(
    StatColumnDef("PTS",  StatField.POINTS),
    StatColumnDef("REB",  StatField.REBOUNDS),
    StatColumnDef("AST",  StatField.ASSISTS),
    StatColumnDef("FLS",  StatField.FOULS),
    StatColumnDef("FTA",  StatField.FREE_THROWS_ATTEMPTED),
    StatColumnDef("FTM",  StatField.FREE_THROWS_MADE),
)

// ── Entry point ───────────────────────────────────────────────────────────────

@Composable
fun MatchStatsScreen(
    navigation: INavigationRouter,
    viewModel: MatchStatsViewModel = hiltViewModel(),
) {
    val uiState = viewModel.uiState

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            navigation.returnBack()
            viewModel.onSavedConsumed()
        }
    }

    MatchStatsScreenContent(
        uiState = uiState,
        actions = viewModel,
        onBack = navigation::returnBack,
    )
}

// ── Content composable ────────────────────────────────────────────────────────

@Composable
internal fun MatchStatsScreenContent(
    uiState: MatchStatsScreenUiState,
    actions: MatchStatsScreenActions,
    onBack: () -> Unit,
) {
    BaseScreen(
        title = stringResource(R.string.match_stats_title),
        onBack = onBack,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                // Shared horizontal scroll state so header and all rows scroll together
                val sharedScrollState = rememberScrollState()

                // Score card
                ScoreCard(
                    myTeamScore = uiState.myTeamScore,
                    opponentScore = uiState.opponentScore,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                )

                // Opponent scoring stepper row
                OpponentScoringRow(
                    opponentScore = uiState.opponentScore,
                    onDelta = actions::onOpponentScoreChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Column headers row — avatar column fixed, stat headers share scroll state
                StatHeaderRow(
                    scrollState = sharedScrollState,
                    modifier = Modifier.fillMaxWidth(),
                )

                // Player rows — lazy, each row's stats scroll with the shared state
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(0.dp),
                ) {
                    items(uiState.players, key = { it.teamMemberId }) { player ->
                        PlayerStatRow(
                            player = player,
                            scrollState = sharedScrollState,
                            onStatChanged = actions::onStatChanged,
                        )
                    }
                }
            }

            // Error message
            uiState.error?.let { resId ->
                Text(
                    text = stringResource(resId),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    textAlign = TextAlign.Center,
                )
            }

            // Save button pinned above nav bar
            KourtButton(
                text = stringResource(R.string.match_stats_save_button),
                onClick = actions::onSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                enabled = !uiState.isSaving,
            )
        }
    }
}

// ── Score card ────────────────────────────────────────────────────────────────

@Composable
private fun ScoreCard(
    myTeamScore: Int,
    opponentScore: Int,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            // My team side
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.match_stats_my_team_label),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    letterSpacing = 1.sp,
                )
                Text(
                    text = myTeamScore.toString(),
                    style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Text(
                text = stringResource(R.string.match_stats_vs_label),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            )

            // Opponent side
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.match_stats_opponent_label),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    letterSpacing = 1.sp,
                )
                Text(
                    text = opponentScore.toString(),
                    style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

// ── Opponent scoring row ──────────────────────────────────────────────────────

@Composable
private fun OpponentScoringRow(
    opponentScore: Int,
    onDelta: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            painter = painterResource(R.drawable.shield),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.size(20.dp),
        )
        Text(
            text = stringResource(R.string.match_stats_opponent_scoring_label),
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.weight(1f),
        )
        StatStepper(
            value = opponentScore,
            onDelta = onDelta,
            width = StatColumnWidth,
        )
    }
}

// ── Header row ────────────────────────────────────────────────────────────────

@Composable
private fun StatHeaderRow(
    scrollState: androidx.compose.foundation.ScrollState,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .padding(bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Blank avatar column placeholder
        Spacer(modifier = Modifier.width(AvatarColumnWidth))

        // Scrollable header labels — share state with player rows
        Row(
            modifier = Modifier.horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            statColumns.forEach { col ->
                Text(
                    text = col.label,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(StatColumnWidth),
                )
            }
        }
    }
}

// ── Player row ────────────────────────────────────────────────────────────────

@Composable
private fun PlayerStatRow(
    player: PlayerStatEntryUiItem,
    scrollState: androidx.compose.foundation.ScrollState,
    onStatChanged: (teamMemberId: String, StatField, Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Fixed avatar column with jersey badge overlay
        Box(
            modifier = Modifier
                .width(AvatarColumnWidth)
                .padding(end = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            KourtAvatarLeading(
                fallbackText = player.displayName,
                photoUrl = player.avatarUrl,
                size = 48.dp,
            )
            // Jersey number badge at bottom-left of avatar
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .align(Alignment.BottomStart),
            ) {
                Text(
                    text = player.jerseyNumber.toString(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                    ),
                    color = MaterialTheme.colorScheme.onPrimary,
                    textAlign = TextAlign.Center,
                )
            }
        }

        // Scrollable stat steppers — share state with header
        Row(
            modifier = Modifier.horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            statColumns.forEach { col ->
                val value = when (col.field) {
                    StatField.POINTS               -> player.points
                    StatField.REBOUNDS             -> player.rebounds
                    StatField.ASSISTS              -> player.assists
                    StatField.FOULS                -> player.fouls
                    StatField.FREE_THROWS_ATTEMPTED -> player.freeThrowsAttempted
                    StatField.FREE_THROWS_MADE     -> player.freeThrowsMade
                }
                StatStepper(
                    value = value,
                    onDelta = { delta -> onStatChanged(player.teamMemberId, col.field, delta) },
                    width = StatColumnWidth,
                )
            }
        }
    }
}

// ── Stat stepper pill ─────────────────────────────────────────────────────────

@Composable
private fun StatStepper(
    value: Int,
    onDelta: (Int) -> Unit,
    modifier: Modifier = Modifier,
    width: Dp = StatColumnWidth,
) {
    Surface(
        modifier = modifier.width(width),
        shape = RoundedCornerShape(50.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "−",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .width(20.dp)
                    .clickable { onDelta(-1) },
            )
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(20.dp),
            )
            Text(
                text = "+",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .width(20.dp)
                    .clickable { onDelta(1) },
            )
        }
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

private val previewPlayers = listOf(
    PlayerStatEntryUiItem(
        userId = "u1", teamMemberId = "m1",
        displayName = "Marcus Johnson", avatarUrl = "",
        jerseyNumber = 7, points = 14, rebounds = 5, assists = 3, fouls = 2,
        freeThrowsAttempted = 4, freeThrowsMade = 3,
    ),
    PlayerStatEntryUiItem(
        userId = "u2", teamMemberId = "m2",
        displayName = "Tyler Brooks", avatarUrl = "",
        jerseyNumber = 23, points = 22, rebounds = 8, assists = 1, fouls = 4,
        freeThrowsAttempted = 6, freeThrowsMade = 4,
    ),
    PlayerStatEntryUiItem(
        userId = "u3", teamMemberId = "m3",
        displayName = "Jamie Lee", avatarUrl = "",
        jerseyNumber = 11, points = 8, rebounds = 3, assists = 7, fouls = 1,
        freeThrowsAttempted = 2, freeThrowsMade = 2,
    ),
    PlayerStatEntryUiItem(
        userId = "u4", teamMemberId = "m4",
        displayName = "Sam Torres", avatarUrl = "",
        jerseyNumber = 4, points = 4, rebounds = 6, assists = 2, fouls = 3,
        freeThrowsAttempted = 0, freeThrowsMade = 0,
    ),
)

private val previewActions = object : MatchStatsScreenActions {
    override fun onOpponentScoreChanged(delta: Int) = Unit
    override fun onStatChanged(teamMemberId: String, stat: StatField, delta: Int) = Unit
    override fun onSave() = Unit
    override fun onSavedConsumed() = Unit
}

@Preview(name = "MatchStatsScreen — dark", showBackground = true)
@Composable
private fun MatchStatsScreenDarkPreview() {
    KourtTheme(darkTheme = true) {
        MatchStatsScreenContent(
            uiState = MatchStatsScreenUiState(
                eventTitle = "vs. Lakers",
                opponentScore = 72,
                players = previewPlayers,
            ),
            actions = previewActions,
            onBack = {},
        )
    }
}

@Preview(name = "MatchStatsScreen — light", showBackground = true)
@Composable
private fun MatchStatsScreenLightPreview() {
    KourtTheme(darkTheme = false) {
        MatchStatsScreenContent(
            uiState = MatchStatsScreenUiState(
                eventTitle = "vs. Lakers",
                opponentScore = 72,
                players = previewPlayers,
            ),
            actions = previewActions,
            onBack = {},
        )
    }
}

@Preview(name = "MatchStatsScreen — loading", showBackground = true)
@Composable
private fun MatchStatsScreenLoadingPreview() {
    KourtTheme(darkTheme = true) {
        MatchStatsScreenContent(
            uiState = MatchStatsScreenUiState(isLoading = true),
            actions = previewActions,
            onBack = {},
        )
    }
}
