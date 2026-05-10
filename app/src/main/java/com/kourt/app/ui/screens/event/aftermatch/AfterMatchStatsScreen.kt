package com.kourt.app.ui.screens.event.aftermatch

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import com.kourt.app.R
import com.kourt.app.navigation.Destination
import com.kourt.app.navigation.INavigationRouter
import com.kourt.app.ui.components.BaseScreenWithBottomNav
import com.kourt.app.ui.components.KourtAvatarLeading
import com.kourt.app.ui.components.MvpCard
import com.kourt.app.ui.theme.KourtTheme
import com.kourt.app.ui.theme.OrangeDark
import com.kourt.app.ui.theme.OrangeLight
import com.kourt.app.viewmodel.event.AfterMatchStatsViewModel

// ── Constants ─────────────────────────────────────────────────────────────────

private val NameColumnWidth  = 130.dp
private val StatColumnWidth  = 56.dp

private data class ReadStatCol(val label: String, val value: (PlayerStatRowItem) -> String)

private val readStatColumns = listOf(
    ReadStatCol("PTS")  { it.points.toString() },
    ReadStatCol("REB")  { it.rebounds.toString() },
    ReadStatCol("AST")  { it.assists.toString() },
    ReadStatCol("FLS")  { it.fouls.toString() },
    ReadStatCol("FTA")  { it.freeThrowsAttempted.toString() },
    ReadStatCol("FTM")  { it.freeThrowsMade.toString() },
)

// ── Entry point ───────────────────────────────────────────────────────────────

@Composable
fun AfterMatchStatsScreen(
    navigation: INavigationRouter,
    viewModel: AfterMatchStatsViewModel = hiltViewModel(),
) {
    val uiState = viewModel.uiState
    val navBackStackEntry by navigation.getNavController().currentBackStackEntryAsState()

    LaunchedEffect(navBackStackEntry) {
        if (navBackStackEntry?.destination?.route == Destination.AfterMatchStatsScreen.route) {
            viewModel.loadData()
        }
    }

    AfterMatchStatsScreenContent(
        uiState = uiState,
        onBack = navigation::returnBack,
        onEditStats = { navigation.navigateToMatchStats(viewModel.eventId) },
    )
}

// ── Content composable ────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AfterMatchStatsScreenContent(
    uiState: AfterMatchStatsScreenUiState,
    onBack: () -> Unit,
    onEditStats: () -> Unit = {},
) {
    val outlineColor = MaterialTheme.colorScheme.surface

    BaseScreenWithBottomNav(
        topBar = {
            CenterAlignedTopAppBar(
                modifier = Modifier.drawWithContent {
                    drawContent()
                    val thickness = 1.dp.toPx()
                    val y = size.height - (thickness / 2)
                    drawLine(
                        color = outlineColor,
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = thickness,
                    )
                },
                title = {
                    val titleText = if (uiState.opponentName.isNotBlank() && uiState.eventDate.isNotBlank()) {
                        "Match (${uiState.opponentName}) - ${uiState.eventDate}"
                    } else {
                        stringResource(R.string.after_match_stats_title)
                    }
                    Text(
                        text = titleText,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.after_match_stats_back_cd),
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onEditStats) {
                        Icon(
                            painter = painterResource(R.drawable.edit),
                            contentDescription = stringResource(R.string.after_match_stats_edit_cd),
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface,
                ),
            )
        },
        bottomBar = {},
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            return@BaseScreenWithBottomNav
        }

        uiState.error?.let { resId ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(resId),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp),
                )
            }
            return@BaseScreenWithBottomNav
        }

        val sharedScrollState = rememberScrollState()

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            // ── Score card ─────────────────────────────────────────────────────
            item {
                ResultScoreCard(
                    myTeamScore = uiState.myTeamScore,
                    opponentScore = uiState.opponentScore,
                    opponentName = uiState.opponentName,
                    isWin = uiState.isWin,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                )
            }

            // ── MVP card ───────────────────────────────────────────────────────
            uiState.mvpItem?.let { mvp ->
                item {
                    MvpCard(
                        player = mvp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            // ── Player stats header ────────────────────────────────────────────
            item {
                PlayerStatsHeader(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                )
            }

            // ── Column header row ──────────────────────────────────────────────
            item {
                ReadStatHeaderRow(
                    scrollState = sharedScrollState,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            // ── Player rows ────────────────────────────────────────────────────
            items(uiState.players, key = { it.teamMemberId }) { player ->
                ReadPlayerStatRow(
                    player = player,
                    scrollState = sharedScrollState,
                )
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

// ── Result score card ─────────────────────────────────────────────────────────

@Composable
private fun ResultScoreCard(
    myTeamScore: Int,
    opponentScore: Int,
    opponentName: String,
    isWin: Boolean,
    modifier: Modifier = Modifier,
) {
    val isDark = MaterialTheme.colorScheme.background.blue < 0.5f
    val accentColor = if (isDark) OrangeDark else OrangeLight

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                // My team
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
                        color = accentColor,
                    )
                }

                // Colon separator
                Text(
                    text = ":",
                    style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                )

                // Opponent
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = opponentName.uppercase(),
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

            // WIN / LOSS badge
            val (badgeText, badgeBg, badgeFg) = if (isWin) {
                Triple(
                    stringResource(R.string.after_match_stats_win_badge),
                    Color(0xFF16A34A),
                    Color.White,
                )
            } else {
                Triple(
                    stringResource(R.string.after_match_stats_loss_badge),
                    Color(0xFFDC2626),
                    Color.White,
                )
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(badgeBg)
                    .padding(horizontal = 20.dp, vertical = 6.dp),
            ) {
                Text(
                    text = badgeText,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp,
                    ),
                    color = badgeFg,
                )
            }
        }
    }
}

// ── Player stats section header ───────────────────────────────────────────────

@Composable
private fun PlayerStatsHeader(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(R.string.after_match_stats_player_stats_title),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
        )
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .clip(RoundedCornerShape(50.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 10.dp, vertical = 4.dp),
        ) {
            Text(
                text = stringResource(R.string.after_match_stats_top_performers),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                ),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            )
        }
    }
}

// ── Read-only stat header row ─────────────────────────────────────────────────

@Composable
private fun ReadStatHeaderRow(
    scrollState: androidx.compose.foundation.ScrollState,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .padding(bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Fixed name column placeholder
        Spacer(modifier = Modifier.width(NameColumnWidth))

        // Scrollable stat headers
        Row(
            modifier = Modifier.horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            readStatColumns.forEach { col ->
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

// ── Read-only player row ──────────────────────────────────────────────────────

@Composable
private fun ReadPlayerStatRow(
    player: PlayerStatRowItem,
    scrollState: androidx.compose.foundation.ScrollState,
    modifier: Modifier = Modifier,
) {
    val isDark = MaterialTheme.colorScheme.background.blue < 0.5f
    val accentColor = if (isDark) OrangeDark else OrangeLight

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Fixed name column: avatar + name
        Row(
            modifier = Modifier.width(NameColumnWidth),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            KourtAvatarLeading(
                fallbackText = player.displayName,
                photoUrl = player.avatarUrl,
                size = 36.dp,
            )
            Text(
                text = player.displayName,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                modifier = Modifier.weight(1f),
            )
        }

        // Scrollable stat values
        Row(
            modifier = Modifier.horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            readStatColumns.forEachIndexed { index, col ->
                val valueText = col.value(player)
                // Highlight the first 3 columns (PTS, REB, AST) in accent color
                val textColor = if (index < 3) accentColor else MaterialTheme.colorScheme.onSurface
                Text(
                    text = valueText,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = textColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(StatColumnWidth),
                )
            }
        }
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

private val previewMvp = PlayerStatRowItem(
    teamMemberId = "m1",
    displayName = "Marcus Johnson",
    avatarUrl = "",
    points = 28,
    rebounds = 8,
    assists = 5,
    fouls = 2,
    freeThrowsAttempted = 8,
    freeThrowsMade = 7,
)

private val previewPlayers = listOf(
    previewMvp,
    PlayerStatRowItem(
        teamMemberId = "m2",
        displayName = "Tyler Brooks",
        avatarUrl = "",
        points = 14,
        rebounds = 5,
        assists = 3,
        fouls = 3,
        freeThrowsAttempted = 4,
        freeThrowsMade = 3,
    ),
    PlayerStatRowItem(
        teamMemberId = "m3",
        displayName = "Jamie Lee",
        avatarUrl = "",
        points = 10,
        rebounds = 4,
        assists = 7,
        fouls = 1,
        freeThrowsAttempted = 2,
        freeThrowsMade = 2,
    ),
    PlayerStatRowItem(
        teamMemberId = "m4",
        displayName = "Sam Torres",
        avatarUrl = "",
        points = 6,
        rebounds = 3,
        assists = 2,
        fouls = 4,
        freeThrowsAttempted = 0,
        freeThrowsMade = 0,
    ),
)



@Preview(name = "AfterMatchStats — dark, win", showBackground = true)
@Composable
private fun AfterMatchStatsDarkWinPreview() {
    KourtTheme(darkTheme = true) {
        AfterMatchStatsScreenContent(
            uiState = AfterMatchStatsScreenUiState(
                isLoading = false,
                opponentName = "Celtics",
                eventDate = "Oct 15",
                myTeamScore = 84,
                opponentScore = 72,
                isWin = true,
                mvpItem = previewMvp,
                players = previewPlayers,
            ),
            onBack = {},
        )
    }
}

@Preview(name = "AfterMatchStats — dark, loss", showBackground = true)
@Composable
private fun AfterMatchStatsDarkLossPreview() {
    KourtTheme(darkTheme = true) {
        AfterMatchStatsScreenContent(
            uiState = AfterMatchStatsScreenUiState(
                isLoading = false,
                opponentName = "Lakers",
                eventDate = "Nov 3",
                myTeamScore = 68,
                opponentScore = 91,
                isWin = false,
                mvpItem = previewMvp,
                players = previewPlayers,
            ),
            onBack = {},
        )
    }
}

@Preview(name = "AfterMatchStats — light, win", showBackground = true)
@Composable
private fun AfterMatchStatsLightWinPreview() {
    KourtTheme(darkTheme = false) {
        AfterMatchStatsScreenContent(
            uiState = AfterMatchStatsScreenUiState(
                isLoading = false,
                opponentName = "Celtics",
                eventDate = "Oct 15",
                myTeamScore = 84,
                opponentScore = 72,
                isWin = true,
                mvpItem = previewMvp,
                players = previewPlayers,
            ),
            onBack = {},
        )
    }
}

@Preview(name = "AfterMatchStats — loading", showBackground = true)
@Composable
private fun AfterMatchStatsLoadingPreview() {
    KourtTheme(darkTheme = true) {
        AfterMatchStatsScreenContent(
            uiState = AfterMatchStatsScreenUiState(isLoading = true),
            onBack = {},
        )
    }
}
