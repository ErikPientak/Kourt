package com.kourt.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kourt.app.R
import com.kourt.app.ui.screens.dashboard.coach.EventUiItem


@Composable
fun UpNextCard(
    event: EventUiItem,
    rosterSize: Int,
    attendeeNames: List<String>,
    onUpdateLineup: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(52.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column {
            // Header — background image with dark overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .background(Color(0xFF1A2635)),
            ) {
                Image(
                    painter = painterResource(R.drawable.court2),
                    contentDescription = null,
                    modifier = Modifier
                        .matchParentSize(),
                    contentScale = ContentScale.Crop,
                )
                
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color.Black.copy(alpha = 0.4f)),
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50.dp))
                                .background(MaterialTheme.colorScheme.primary)
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                        ) {
                            Text(
                                text = event.type.uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White,
                            )
                        }
                        Text(
                            text = event.dateLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.75f),
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = event.title,
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            text = event.startTime,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color.White,
                        )
                    }
                }
            }

            // Body
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.location),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp),
                    )
                    Text(
                        text = event.location.ifBlank { stringResource(R.string.coach_dashboard_location_tbd) },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                AttendanceSection(
                    rsvpYes = event.rsvpYes,
                    rsvpMaybe = event.rsvpMaybe,
                    rsvpNo = event.rsvpNo,
                    rosterSize = rosterSize,
                    attendeeNames = attendeeNames,
                )

                KourtButton(
                    text = stringResource(R.string.coach_dashboard_update_lineup),
                    onClick = onUpdateLineup,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun AttendanceSection(
    rsvpYes: Int,
    rsvpMaybe: Int,
    rsvpNo: Int,
    rosterSize: Int,
    attendeeNames: List<String>,
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = stringResource(R.string.coach_dashboard_attendance),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                    ),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                )
                AvatarStack(
                    names = attendeeNames.take(3),
                    extraCount = (attendeeNames.size - 3).coerceAtLeast(0),
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                AttendanceDonut(
                    yes = rsvpYes,
                    maybe = rsvpMaybe,
                    no = rsvpNo,
                    total = rosterSize,
                )
                AttendanceLegend(
                    yes = rsvpYes,
                    maybe = rsvpMaybe,
                    no = rsvpNo,
                )
            }
        }
    }
}

@Composable
private fun AvatarStack(names: List<String>, extraCount: Int) {
    val avatarSize = 36.dp
    val overlapStep = 24.dp
    val displayCount = names.size + if (extraCount > 0) 1 else 0
    val totalWidth = if (displayCount == 0) avatarSize
    else avatarSize + overlapStep * (displayCount - 1)
    val borderColor = MaterialTheme.colorScheme.surface

    Box(
        modifier = Modifier
            .height(avatarSize)
            .width(totalWidth),
    ) {
        names.forEachIndexed { index, name ->
            KourtAvatarLeading(
                fallbackText = name,
                size = avatarSize,
                modifier = Modifier
                    .offset(x = overlapStep * index)
                    .border(1.5.dp, borderColor, CircleShape),
            )
        }
        if (extraCount > 0) {
            Box(
                modifier = Modifier
                    .offset(x = overlapStep * names.size)
                    .size(avatarSize)
                    .border(1.5.dp, borderColor, CircleShape)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "+$extraCount",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
private fun AttendanceDonut(yes: Int, maybe: Int, no: Int, total: Int) {
    val safeTotal = total.coerceAtLeast(1).toFloat()
    val yesSweep = (yes / safeTotal) * 360f
    val maybeSweep = (maybe / safeTotal) * 360f
    val noSweep = (no / safeTotal) * 360f

    val trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
    val textColor = MaterialTheme.colorScheme.onSurface

    Box(
        modifier = Modifier.size(64.dp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val strokeWidth = 7.dp.toPx()
            val inset = strokeWidth / 2f
            val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
            val topLeft = Offset(inset, inset)
            val strokeStyle = Stroke(strokeWidth, cap = StrokeCap.Butt)

            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = strokeStyle,
            )

            var startAngle = -90f
            if (yesSweep > 0f) {
                drawArc(
                    color = Color(0xFF22C55E),
                    startAngle = startAngle,
                    sweepAngle = yesSweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = strokeStyle,
                )
                startAngle += yesSweep
            }
            if (maybeSweep > 0f) {
                drawArc(
                    color = Color(0xFFF97316),
                    startAngle = startAngle,
                    sweepAngle = maybeSweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = strokeStyle,
                )
                startAngle += maybeSweep
            }
            if (noSweep > 0f) {
                drawArc(
                    color = Color(0xFFEF4444),
                    startAngle = startAngle,
                    sweepAngle = noSweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = strokeStyle,
                )
            }
        }

        Text(
            text = "$yes/$total",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = textColor,
        )
    }
}

@Composable
private fun AttendanceLegend(yes: Int, maybe: Int, no: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        LegendRow(color = Color(0xFF22C55E), label = stringResource(R.string.coach_dashboard_rsvp_yes), count = yes)
        LegendRow(color = Color(0xFFF97316), label = stringResource(R.string.coach_dashboard_rsvp_maybe), count = maybe)
        LegendRow(color = Color(0xFFEF4444), label = stringResource(R.string.coach_dashboard_rsvp_no), count = no)
    }
}

@Composable
private fun LegendRow(color: Color, label: String, count: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color),
        )
        Text(
            text = "$label ($count)",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
        )
    }
}

