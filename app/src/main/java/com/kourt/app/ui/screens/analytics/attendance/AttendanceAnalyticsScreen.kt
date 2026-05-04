package com.kourt.app.ui.screens.analytics.attendance

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
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
import com.kourt.app.viewmodel.analytics.AttendanceAnalyticsViewModel

// ── Design tokens ─────────────────────────────────────────────────────────────

private val BarLowColor  = Color(0xFF7C3011)
private val BarHighColor = Color(0xFF16A34A)

private val ExcellentColor     = Color(0xFF16A34A)
private val ConsistentColor    = Color(0xFFF59E0B)
private val NeedsAttentionColor = Color(0xFFDC2626)

private val PositiveTrendColor = Color(0xFF16A34A)
private val NegativeTrendColor = Color(0xFFDC2626)

// ── Entry point ───────────────────────────────────────────────────────────────

@Composable
fun AttendanceAnalyticsScreen(
    navigation: INavigationRouter,
    viewModel: AttendanceAnalyticsViewModel = hiltViewModel(),
) {
    val uiState = viewModel.uiState

    LaunchedEffect(uiState.error) {
        // Error is surfaced inline — no navigation side-effect needed.
    }

    AttendanceAnalyticsScreenContent(
        uiState = uiState,
        onBack = { navigation.returnBack() },
    )
}

// ── Content ───────────────────────────────────────────────────────────────────

@Composable
fun AttendanceAnalyticsScreenContent(
    uiState: AttendanceAnalyticsScreenUiState,
    onBack: () -> Unit,
) {
    val isDark = MaterialTheme.colorScheme.background.blue < 0.5f
    val accent = if (isDark) OrangeDark else OrangeLight

    BaseScreen(
        title = stringResource(R.string.attendance_analytics_title),
        onBack = onBack,
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

            uiState.teamAttendanceRate == 0f -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
                ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = stringResource(R.string.attendance_analytics_no_data),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                }
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
                        AttendanceRateCard(
                            teamAttendanceRate = uiState.teamAttendanceRate,
                            teamOnTimeRate     = uiState.teamOnTimeRate,
                            teamLateRate       = uiState.teamLateRate,
                            teamAbsentRate     = uiState.teamAbsentRate,
                            modifier           = Modifier.weight(1f),
                        )
                        MostReliableCard(
                            player   = uiState.mostReliablePlayer,
                            accent   = accent,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                item {
                    TrendCard(
                        bars = uiState.trendBars,
                        trendDelta = uiState.trendDelta,
                        isTrendImproving = uiState.isTrendImproving,
                        accent = accent,
                    )
                }

                item {
                    Text(
                        text = stringResource(R.string.attendance_analytics_player_breakdown),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                }

                items(
                    items = uiState.players,
                    key = { it.teamMemberId },
                ) { player ->
                    PlayerBreakdownRow(player = player, accent = accent)
                }
            }
        }
    }
}

// ── Attendance rate card ──────────────────────────────────────────────────────

private val DonutOnTimeColor  = Color(0xFF16A34A)
private val DonutLateColor    = Color(0xFFF59E0B)
private val DonutAbsentColor  = Color(0xFFDC2626)

@Composable
private fun AttendanceRateCard(
    teamAttendanceRate: Float,
    teamOnTimeRate: Float,
    teamLateRate: Float,
    teamAbsentRate: Float,
    modifier: Modifier = Modifier,
) {
    val pct = (teamAttendanceRate * 100).toInt()
    val trackColor = MaterialTheme.colorScheme.background

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
                text = stringResource(R.string.attendance_analytics_rate_label),
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Medium,
                ),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
            )

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(100.dp),
            ) {
                Canvas(modifier = Modifier.size(100.dp)) {
                    val strokeWidth = 14.dp.toPx()
                    val inset = strokeWidth / 2f
                    val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)

                    // Track
                    drawArc(
                        color = trackColor,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = Offset(inset, inset),
                        size = arcSize,
                        style = Stroke(width = strokeWidth),
                    )

                    // Green — on time
                    val greenSweep = 360f * teamOnTimeRate.coerceIn(0f, 1f)
                    drawArc(
                        color = DonutOnTimeColor,
                        startAngle = -90f,
                        sweepAngle = greenSweep,
                        useCenter = false,
                        topLeft = Offset(inset, inset),
                        size = arcSize,
                        style = Stroke(width = strokeWidth),
                    )

                    // Orange — late
                    val orangeSweep = 360f * teamLateRate.coerceIn(0f, 1f)
                    drawArc(
                        color = DonutLateColor,
                        startAngle = -90f + greenSweep,
                        sweepAngle = orangeSweep,
                        useCenter = false,
                        topLeft = Offset(inset, inset),
                        size = arcSize,
                        style = Stroke(width = strokeWidth),
                    )

                    // Red — absent
                    val redSweep = 360f * teamAbsentRate.coerceIn(0f, 1f)
                    drawArc(
                        color = DonutAbsentColor,
                        startAngle = -90f + greenSweep + orangeSweep,
                        sweepAngle = redSweep,
                        useCenter = false,
                        topLeft = Offset(inset, inset),
                        size = arcSize,
                        style = Stroke(width = strokeWidth),
                    )
                }

                Text(
                    text = "$pct%",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

// ── Most reliable card ────────────────────────────────────────────────────────

@Composable
private fun MostReliableCard(
    player: PlayerAttendanceItem?,
    accent: Color,
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
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(R.string.attendance_analytics_most_reliable),
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
                    text = "${(player.attendanceRate * 100).toInt()}%",
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

// ── Trend card ────────────────────────────────────────────────────────────────

@Composable
private fun TrendCard(
    bars: List<WeeklyAttendanceBar>,
    trendDelta: Float,
    isTrendImproving: Boolean,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    val deltaSign = if (trendDelta >= 0f) "+" else ""
    val deltaPct  = "${deltaSign}${(trendDelta * 100).toInt()}%"
    val deltaColor = if (isTrendImproving) PositiveTrendColor else NegativeTrendColor
    val trendLabel = if (isTrendImproving)
        stringResource(R.string.attendance_analytics_trend_improving)
    else
        stringResource(R.string.attendance_analytics_trend_declining)
    val vsPeriodLabel = stringResource(R.string.attendance_analytics_trend_vs_period, deltaPct)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.attendance_analytics_trend_label),
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Medium,
                ),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            )

            Text(
                text = trendLabel,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
            )

            Text(
                text = vsPeriodLabel,
                style = MaterialTheme.typography.bodySmall,
                color = deltaColor,
            )

            if (bars.isNotEmpty()) {
                AttendanceBarChart(bars = bars)
            }
        }
    }
}

@Composable
private fun AttendanceBarChart(
    bars: List<WeeklyAttendanceBar>,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        bars.forEach { bar ->
            val barColor = lerp(BarLowColor, BarHighColor, bar.rate.coerceIn(0f, 1f))
            val alpha = if (bar.isCurrentWeek) 1f else 0.55f

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .fillMaxWidth()
                        .height((bar.rate * 64).dp)
                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                        .background(barColor.copy(alpha = alpha)),
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = bar.weekLabel,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (bar.isCurrentWeek) 0.8f else 0.4f),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                )
            }
        }
    }
}

// ── Player breakdown ──────────────────────────────────────────────────────────

@Composable
private fun PlayerBreakdownRow(
    player: PlayerAttendanceItem,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    val reliabilityColor = when (player.reliabilityLabel) {
        "EXCELLENT"       -> ExcellentColor
        "CONSISTENT"      -> ConsistentColor
        else              -> NeedsAttentionColor
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // Top row: avatar + name + rate
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
                    text = "${(player.attendanceRate * 100).toInt()}%",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                    ),
                    color = accent,
                )
            }

            // Reliability label row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.attendance_analytics_reliability_score),
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.weight(1f),
                )

                Text(
                    text = player.reliabilityLabel,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = reliabilityColor,
                )
            }

            // 3-segment progress bar
            ThreeSegmentBar(
                onTimeRate = player.onTimeRate,
                lateRate = player.lateRate,
                absentRate = player.absentRate,
            )
        }
    }
}

@Composable
private fun ThreeSegmentBar(
    onTimeRate: Float,
    lateRate: Float,
    absentRate: Float,
    modifier: Modifier = Modifier,
) {
    val green  = ExcellentColor
    val orange = ConsistentColor
    val red    = NeedsAttentionColor

    // Normalize so all three segments always sum to 1f, guarding against
    // floating-point drift and the zero-total edge case.
    val total = (onTimeRate + lateRate + absentRate).coerceAtLeast(0.001f)
    val gW = (onTimeRate / total).coerceAtLeast(0f)
    val oW = (lateRate / total).coerceAtLeast(0f)
    val rW = (absentRate / total).coerceAtLeast(0f)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(50)),
    ) {
        if (gW > 0f) {
            Box(
                modifier = Modifier
                    .weight(gW)
                    .fillMaxHeight()
                    .background(green),
            )
        }
        if (oW > 0f) {
            Box(
                modifier = Modifier
                    .weight(oW)
                    .fillMaxHeight()
                    .background(orange),
            )
        }
        if (rW > 0f) {
            Box(
                modifier = Modifier
                    .weight(rW)
                    .fillMaxHeight()
                    .background(red),
            )
        }
    }
}

// ── Preview data ──────────────────────────────────────────────────────────────

private val previewPlayers = listOf(
    PlayerAttendanceItem(
        teamMemberId     = "m1",
        displayName      = "Marcus Johnson",
        avatarUrl        = "a3",
        attendanceRate   = 0.95f,
        onTimeRate       = 0.78f,
        lateRate         = 0.17f,
        absentRate       = 0.05f,
        reliabilityLabel = "EXCELLENT",
    ),
    PlayerAttendanceItem(
        teamMemberId     = "m2",
        displayName      = "Tyler Brooks",
        avatarUrl        = "a7",
        attendanceRate   = 0.82f,
        onTimeRate       = 0.65f,
        lateRate         = 0.17f,
        absentRate       = 0.18f,
        reliabilityLabel = "CONSISTENT",
    ),
    PlayerAttendanceItem(
        teamMemberId     = "m3",
        displayName      = "Jamie Lee",
        avatarUrl        = "a12",
        attendanceRate   = 0.78f,
        onTimeRate       = 0.60f,
        lateRate         = 0.18f,
        absentRate       = 0.22f,
        reliabilityLabel = "CONSISTENT",
    ),
    PlayerAttendanceItem(
        teamMemberId     = "m4",
        displayName      = "Sam Torres",
        avatarUrl        = "",
        attendanceRate   = 0.65f,
        onTimeRate       = 0.45f,
        lateRate         = 0.20f,
        absentRate       = 0.35f,
        reliabilityLabel = "NEEDS ATTENTION",
    ),
    PlayerAttendanceItem(
        teamMemberId     = "m5",
        displayName      = "Alex Kim",
        avatarUrl        = "",
        attendanceRate   = 0.50f,
        onTimeRate       = 0.30f,
        lateRate         = 0.20f,
        absentRate       = 0.50f,
        reliabilityLabel = "NEEDS ATTENTION",
    ),
)

private val previewBars = listOf(
    WeeklyAttendanceBar(weekLabel = "W15", rate = 0.58f),
    WeeklyAttendanceBar(weekLabel = "W16", rate = 0.62f),
    WeeklyAttendanceBar(weekLabel = "W17", rate = 0.74f),
    WeeklyAttendanceBar(weekLabel = "W18", rate = 0.83f, isCurrentWeek = true),
)

private val previewUiState = AttendanceAnalyticsScreenUiState(
    isLoading          = false,
    teamAttendanceRate = 0.74f,
    mostReliablePlayer = previewPlayers.first(),
    trendBars          = previewBars,
    trendDelta         = 0.09f,
    isTrendImproving   = true,
    players            = previewPlayers,
)

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(name = "Attendance Analytics — dark", showBackground = true, widthDp = 360, heightDp = 900)
@Composable
private fun AttendanceAnalyticsDarkPreview() {
    KourtTheme(darkTheme = true) {
        AttendanceAnalyticsScreenContent(
            uiState = previewUiState,
            onBack = {},
        )
    }
}

@Preview(name = "Attendance Analytics — light", showBackground = true, widthDp = 360, heightDp = 900)
@Composable
private fun AttendanceAnalyticsLightPreview() {
    KourtTheme(darkTheme = false) {
        AttendanceAnalyticsScreenContent(
            uiState = previewUiState,
            onBack = {},
        )
    }
}

