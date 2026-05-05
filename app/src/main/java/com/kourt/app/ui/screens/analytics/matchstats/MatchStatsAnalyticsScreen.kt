package com.kourt.app.ui.screens.analytics.matchstats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kourt.app.R
import com.kourt.app.navigation.INavigationRouter
import com.kourt.app.ui.components.BaseScreen
import com.kourt.app.ui.components.KourtAvatarLeading
import com.kourt.app.ui.theme.KourtTheme
import com.kourt.app.ui.theme.OrangeDark
import com.kourt.app.ui.theme.OrangeLight
import com.kourt.app.viewmodel.analytics.MatchStatsAnalyticsViewModel
import kourtNeonGlow

// ── Entry point ───────────────────────────────────────────────────────────────

@Composable
fun MatchStatsAnalyticsScreen(
    navigation: INavigationRouter,
    viewModel: MatchStatsAnalyticsViewModel = hiltViewModel(),
) {
    MatchStatsAnalyticsScreenContent(
        uiState = viewModel.uiState,
        onBack  = { navigation.returnBack() },
        onPrev  = { navigation.navigateToAttendanceAnalytics() },
    )
}

// ── Content ───────────────────────────────────────────────────────────────────

@Composable
fun MatchStatsAnalyticsScreenContent(
    uiState: MatchStatsAnalyticsScreenUiState,
    onBack: () -> Unit,
    onPrev: () -> Unit = {},
) {
    val isDark = MaterialTheme.colorScheme.background.blue < 0.5f
    val accent = if (isDark) OrangeDark else OrangeLight

    BaseScreen(
        title = stringResource(R.string.match_stats_analytics_title),
        onBack = onBack,
        trailingIcon = {
            IconButton(onClick = onPrev) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = stringResource(R.string.match_stats_analytics_prev_cd),
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }
        },
    ) { padding ->
        when {
            uiState.isLoading -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }

            uiState.error != null -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(uiState.error),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp),
                )
            }

            uiState.players.isEmpty() -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.match_stats_analytics_no_data),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                )
            }

            else -> LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        TeamRecordCard(
                            wins     = uiState.wins,
                            losses   = uiState.losses,
                            draws    = uiState.draws,
                            modifier = Modifier.weight(1f),
                        )
                        TopScorerCard(
                            player   = uiState.topScorer,
                            accent   = accent,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                item {
                    Text(
                        text = stringResource(R.string.match_stats_analytics_player_breakdown),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                }

                if (uiState.players.isNotEmpty()) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column {
                                uiState.players.forEachIndexed { index, player ->
                                    if (index > 0) {
                                        HorizontalDivider(
                                            color = MaterialTheme.colorScheme.background,
                                            thickness = 5.dp,
                                        )
                                    }
                                    PlayerStatsRow(player = player, accent = accent)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Team record card ──────────────────────────────────────────────────────────

@Composable
private fun TeamRecordCard(
    wins: Int,
    losses: Int,
    draws: Int,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.match_stats_analytics_team_record),
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Medium,
                ),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                RecordStatColumn(label = stringResource(R.string.match_stats_analytics_wins), value = wins)
                RecordStatColumn(label = stringResource(R.string.match_stats_analytics_losses), value = losses)
                RecordStatColumn(label = stringResource(R.string.match_stats_analytics_draws), value = draws)
            }
        }
    }
}

@Composable
private fun RecordStatColumn(label: String, value: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 28.sp,
            ),
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                letterSpacing = 1.sp,
                fontWeight = FontWeight.Medium,
            ),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
        )
    }
}

// ── Top scorer card ───────────────────────────────────────────────────────────

@Composable
private fun TopScorerCard(
    player: PlayerMatchStatsItem?,
    accent: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.kourtNeonGlow(
            color = MaterialTheme.colorScheme.primary,
            shapeRadius = 16.dp,
        ),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(R.string.match_stats_analytics_top_scorer),
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Medium,
                ),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
            )

            if (player == null) {
                Text(
                    text = "—",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                )
            } else {
                KourtAvatarLeading(
                    fallbackText = player.displayName,
                    photoUrl = player.avatarUrl,
                    size = 64.dp,
                )
                Text(
                    text = player.displayName,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                )
                Text(
                    text = "${player.totalPoints} ${stringResource(R.string.match_stats_analytics_pts)}",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp,
                    ),
                    color = accent,
                )
            }
        }
    }
}

// ── Player stats row ──────────────────────────────────────────────────────────

@Composable
private fun PlayerStatsRow(
    player: PlayerMatchStatsItem,
    accent: androidx.compose.ui.graphics.Color,
) {
    val ftText = player.freeThrowPct
        ?.let { "${(it * 100).toInt()}%" }
        ?: "—"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            KourtAvatarLeading(
                fallbackText = player.displayName,
                photoUrl = player.avatarUrl,
                size = 40.dp,
            )
            Text(
                text = player.displayName,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
                maxLines = 1,
            )
            Text(
                text = player.totalPoints.toString(),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                ),
                color = accent,
            )
            Text(
                text = stringResource(R.string.match_stats_analytics_pts),
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Medium,
                ),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 52.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            StatChip(label = stringResource(R.string.match_stats_analytics_reb), value = player.totalRebounds.toString())
            StatChip(label = stringResource(R.string.match_stats_analytics_ast), value = player.totalAssists.toString())
            StatChip(label = stringResource(R.string.match_stats_analytics_ft), value = ftText)
            StatChip(label = stringResource(R.string.match_stats_analytics_fouls), value = player.totalFouls.toString())
        }
    }
}

@Composable
private fun StatChip(label: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                letterSpacing = 0.5.sp,
            ),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
        )
    }
}

// ── Preview data ──────────────────────────────────────────────────────────────

private val previewPlayers = listOf(
    PlayerMatchStatsItem("m1", "Marcus Johnson", "", 340, 82, 45, 28, 0.78f),
    PlayerMatchStatsItem("m2", "Layla Chen",     "", 280, 110, 91, 15, 0.65f),
    PlayerMatchStatsItem("m3", "Derek Williams", "", 220, 68,  33, 42, null),
    PlayerMatchStatsItem("m4", "Sofia Martinez", "", 175, 55,  60, 19, 0.88f),
)

private val previewUiState = MatchStatsAnalyticsScreenUiState(
    isLoading     = false,
    wins          = 5,
    losses        = 2,
    draws         = 1,
    matchesPlayed = 8,
    topScorer     = previewPlayers.first(),
    players       = previewPlayers,
)

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(name = "Match Stats Analytics — dark", showBackground = true, widthDp = 360, heightDp = 900)
@Composable
private fun MatchStatsAnalyticsDarkPreview() {
    KourtTheme(darkTheme = true) {
        MatchStatsAnalyticsScreenContent(
            uiState = previewUiState,
            onBack = {},
            onPrev = {},
        )
    }
}

@Preview(name = "Match Stats Analytics — light", showBackground = true, widthDp = 360, heightDp = 900)
@Composable
private fun MatchStatsAnalyticsLightPreview() {
    KourtTheme(darkTheme = false) {
        MatchStatsAnalyticsScreenContent(
            uiState = previewUiState,
            onBack = {},
            onPrev = {},
        )
    }
}

@Preview(name = "Match Stats Analytics — empty", showBackground = true, widthDp = 360)
@Composable
private fun MatchStatsAnalyticsEmptyPreview() {
    KourtTheme(darkTheme = true) {
        MatchStatsAnalyticsScreenContent(
            uiState = MatchStatsAnalyticsScreenUiState(isLoading = false),
            onBack = {},
            onPrev = {},
        )
    }
}
