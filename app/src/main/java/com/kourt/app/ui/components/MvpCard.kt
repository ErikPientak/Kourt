package com.kourt.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kourt.app.R
import com.kourt.app.ui.screens.event.aftermatch.PlayerStatRowItem
import com.kourt.app.ui.theme.OrangeDark
import com.kourt.app.ui.theme.OrangeLight
import kourtNeonGlow

@Composable
fun MvpCard(
    player: PlayerStatRowItem,
    modifier: Modifier = Modifier,
) {
    val isDark = MaterialTheme.colorScheme.background.blue < 0.5f
    val accentColor = if (isDark) OrangeDark else OrangeLight

    Surface(
        modifier = modifier
            .kourtNeonGlow(
                color = accentColor,
                shapeRadius = 16.dp,)
            .border(
            width = 1.5.dp,
            color = accentColor.copy(alpha = 0.7f),
            shape = RoundedCornerShape(16.dp),
        ),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(accentColor.copy(alpha = 0.08f), Color.Transparent),
                        radius = 700f,
                    )
                )
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // ── Left: avatar with orange ring + MVP badge ──────────────────────
            Box(contentAlignment = Alignment.BottomCenter) {
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .border(2.dp, accentColor, CircleShape)
                        .padding(3.dp)
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    KourtAvatarLeading(
                        fallbackText = player.displayName,
                        photoUrl = player.avatarUrl,
                        size = 82.dp,
                    )
                }
            }

            // ── Right: label + name + stats ────────────────────────────────────
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = stringResource(R.string.after_match_stats_player_of_match),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp,
                    ),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                )
                Text(
                    text = player.displayName,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    MvpStatChip(value = player.points, label = "PTS", accentColor = accentColor)
                    MvpStatChip(value = player.rebounds, label = "REB", accentColor = accentColor)
                    MvpStatChip(value = player.assists, label = "AST", accentColor = accentColor)
                }
            }
        }
    }
}

@Composable
private fun MvpStatChip(
    value: Int,
    label: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
            color = accentColor,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            letterSpacing = 1.sp,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
fun MvpCardNeonPreview() {
    // Mock Data
    val mockPlayer = PlayerStatRowItem(
        displayName = "LeBron James",
        avatarUrl = "a1",
        points = 32,
        rebounds = 12,
        assists = 8,
        fouls = 3,
        teamMemberId = "null",
        freeThrowsAttempted = 0,
        freeThrowsMade = 0
    )

    MaterialTheme(colorScheme = darkColorScheme()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF121212))
                .padding(16.dp)
        ) {
            MvpCard(player = mockPlayer)
        }
    }
}