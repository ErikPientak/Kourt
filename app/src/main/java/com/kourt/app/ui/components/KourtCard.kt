package com.kourt.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.core.graphics.toColorInt
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kourt.app.R
import com.kourt.app.ui.theme.KourtTheme
import com.kourt.app.ui.theme.RoleBadgeAdmin
import com.kourt.app.ui.theme.RoleBadgeAssistant
import com.kourt.app.ui.theme.RoleBadgeCaptain
import com.kourt.app.ui.theme.RoleBadgeCoach
import com.kourt.app.ui.theme.RoleBadgeParent
import com.kourt.app.ui.theme.RoleBadgePlayer
import com.kourt.app.ui.theme.White

/**
 * Unified list-item card used for teams, members, and events.
 *
 * The [leadingContent] slot receives either [KourtAvatarLeading] (team/member)
 * or [KourtDateLeading] (event). All optional decoration defaults to off.
 *
 * The card is only clickable when [onClick] is non-null, keeping
 * non-interactive uses ripple-free.
 */
@Composable
fun KourtCard(
    title: String,
    subtitle: String,
    leadingContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    badge: String? = null,
    badgeColor: Color = Color.Transparent,
    titleIndicatorColor: Color? = null,
    subtitleLeadingIcon: ImageVector? = null,
    subtitleLeadingPainter: Painter? = null,
    titleTextDecoration: TextDecoration? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    val clickableModifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .then(clickableModifier),
        shape = RoundedCornerShape(52.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            leadingContent()

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                // Title row: optional dot → title text → optional badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    if (titleIndicatorColor != null) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(titleIndicatorColor),
                        )
                    }

                    val titleColor = MaterialTheme.colorScheme.onSurface
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = titleColor,
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .then(
                                if (titleTextDecoration == TextDecoration.LineThrough) {
                                    Modifier.drawWithContent {
                                        drawContent()
                                        drawLine(
                                            color = titleColor,
                                            start = Offset(0f, size.height / 2f),
                                            end = Offset(size.width, size.height / 2f),
                                            strokeWidth = 2.dp.toPx(),
                                        )
                                    }
                                } else Modifier
                            ),
                    )

                    if (badge != null) {
                        KourtBadgePill(label = badge, color = badgeColor)
                    }
                }

                // Subtitle row: optional leading icon → subtitle text
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    when {
                        subtitleLeadingPainter != null -> Icon(
                            painter = subtitleLeadingPainter,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.size(14.dp),
                        )
                        subtitleLeadingIcon != null -> Icon(
                            imageVector = subtitleLeadingIcon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.size(14.dp),
                        )
                    }

                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    )
                }
            }

            trailingContent?.invoke()
        }
    }
}

// ── Private helpers ───────────────────────────────────────────────────────────

@Composable
private fun KourtBadgePill(label: String, color: Color) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color,
        )
    }
}

private fun eventTypeColor(type: String): Color = when (type.lowercase()) {
    "match"    -> Color(0xFFFF5A25)
    "practice" -> Color(0xFF2563EB)
    else       -> Color(0xFF6B7280)
}

// ── Leading content helpers ───────────────────────────────────────────────────

/**
 * Colored circle avatar for team cards, using the team's stored [accentColor] and [initials].
 *
 * Falls back to neutral gray when [accentColor] is blank or unparseable.
 */
@Composable
fun KourtTeamAvatar(
    initials: String,
    accentColor: String,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
) {
    val color = remember(accentColor) {
        runCatching { Color(accentColor.toColorInt()) }.getOrDefault(Color(0xFF9CA3AF))
    }
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(color),
    ) {
        Text(
            text = initials.take(3).ifEmpty { "?" },
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = color.contentColorForBackground(),
        )
    }
}

/**
 * Circular avatar for team and member cards.
 *
 * Displays up to two initials derived from [fallbackText] on a
 * [MaterialTheme.colorScheme.background] circle. To add photo support,
 * replace the inner [Box] with a Coil [AsyncImage] keeping the same
 * [size] and [CircleShape] clip.
 */
@Composable
fun KourtAvatarLeading(
    fallbackText: String,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
) {
    val initials = fallbackText
        .trim()
        .split(" ")
        .filter { it.isNotEmpty() }
        .take(2)
        .joinToString("") { it.first().uppercaseChar().toString() }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.background),
    ) {
        Text(
            text = initials.ifEmpty { "?" },
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
        )
    }
}

/**
 * Vertically stacked date block for event cards.
 *
 * Fixed at 44.dp to stay aligned with [KourtAvatarLeading]'s default size.
 */
@Composable
fun KourtDateLeading(
    dayOfWeek: String,
    dayOfMonth: Int,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
) {
    val hasColor = color != Color.Unspecified
    val bgColor = if (hasColor) color.copy(alpha = 0.15f) else MaterialTheme.colorScheme.outline
    val labelColor = if (hasColor) color.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
    val numberColor = if (hasColor) color else MaterialTheme.colorScheme.onBackground

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .size(44.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor),
    ) {
        Text(
            text = dayOfWeek.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = White,
        )
        Text(
            text = dayOfMonth.toString(),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = White,
        )
    }
}

// ── Typed wrappers ────────────────────────────────────────────────────────────

/**
 * [KourtCard] pre-configured for a Team.
 *
 * Leading: circular avatar with team-name initials.
 * Subtitle prefix: whistle icon (head coach).
 * Trailing: chevron when [showChevron] and [onClick] are both set.
 */
@Composable
fun TeamCard(
    teamName: String,
    headCoach: String,
    modifier: Modifier = Modifier,
    accentColor: String = "",
    initials: String = "",
    showChevron: Boolean = true,
    onClick: (() -> Unit)? = null,
) {
    KourtCard(
        title = teamName,
        subtitle = headCoach,
        leadingContent = {
            if (accentColor.isNotBlank()) {
                KourtTeamAvatar(initials = initials, accentColor = accentColor)
            } else {
                KourtAvatarLeading(fallbackText = teamName)
            }
        },
        modifier = modifier,
        subtitleLeadingPainter = painterResource(R.drawable.whistle),
        trailingContent = if (showChevron && onClick != null) {
            {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    modifier = Modifier.size(20.dp),
                )
            }
        } else null,
        onClick = onClick,
    )
}

/**
 * [KourtCard] pre-configured for a TeamMember.
 *
 * Leading: circular avatar with player-name initials.
 * Badge: role label in its role-specific color.
 * Trailing: three-dot menu icon when [showMenu] is true.
 */
@Composable
fun MemberCard(
    playerName: String,
    role: String,
    teamName: String,
    modifier: Modifier = Modifier,
    showMenu: Boolean = false,
    onMenuClick: (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    KourtCard(
        title = playerName,
        subtitle = teamName,
        leadingContent = { KourtAvatarLeading(fallbackText = playerName) },
        modifier = modifier,
        badge = role.uppercase(),
        badgeColor = roleBadgeColor(role),
        trailingContent = if (showMenu) {
            {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More options",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier
                        .size(20.dp)
                        .then(
                            if (onMenuClick != null) Modifier.clickable(onClick = onMenuClick)
                            else Modifier
                        ),
                )
            }
        } else null,
        onClick = onClick,
    )
}

/**
 * [KourtCard] pre-configured for an Event.
 *
 * Leading: date block showing abbreviated day-of-week and day number.
 * Title prefix: colored dot indicating event type (match / practice).
 * Subtitle prefix: clock icon.
 * Trailing: chevron when [showChevron] and [onClick] are both set.
 */
@Composable
fun EventCard(
    title: String,
    eventType: String,
    status: String = "scheduled",
    dayOfWeek: String,
    dayOfMonth: Int,
    startTime: String,
    location: String,
    modifier: Modifier = Modifier,
    showChevron: Boolean = true,
    onClick: (() -> Unit)? = null,
) {
    val isCancelled = status.lowercase() == "cancelled"
    val isPast = status.lowercase() in setOf("past", "completed")

    val cardAlpha = when {
        isCancelled -> 0.80f
        isPast      -> 0.80f
        else        -> 1f
    }

    KourtCard(
        title = title,
        subtitle = if (location.isNotBlank()) "$startTime · $location" else startTime,
        leadingContent = {
            KourtDateLeading(
                dayOfWeek = dayOfWeek,
                dayOfMonth = dayOfMonth,
                color = if (isCancelled) Color.Unspecified else eventTypeColor(eventType),
            )
        },
        modifier = modifier.alpha(cardAlpha),
        titleIndicatorColor = if (isCancelled) null else eventTypeColor(eventType),
        titleTextDecoration = if (isCancelled) TextDecoration.LineThrough else null,
        subtitleLeadingIcon = Icons.Filled.DateRange,
        trailingContent = if (showChevron && onClick != null) {
            {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    modifier = Modifier.size(20.dp),
                )
            }
        } else null,
        onClick = onClick,
    )
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(name = "TeamCard — dark", showBackground = true)
@Composable
private fun TeamCardDarkPreview() {
    KourtTheme(darkTheme = true) {
        TeamCard(
            teamName = "Senior Men",
            headCoach = "Marcus Johnson",
            modifier = Modifier.padding(16.dp),
            onClick = {},
        )
    }
}

@Preview(name = "TeamCard — light", showBackground = true)
@Composable
private fun TeamCardLightPreview() {
    KourtTheme(darkTheme = false) {
        TeamCard(
            teamName = "Senior Men",
            headCoach = "Marcus Johnson",
            modifier = Modifier.padding(16.dp),
            onClick = {},
        )
    }
}

@Preview(name = "MemberCard all roles — dark", showBackground = true)
@Composable
private fun MemberCardRolesDarkPreview() {
    KourtTheme(darkTheme = true) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            listOf("player", "coach", "captain", "assistant", "parent", "admin").forEach { role ->
                MemberCard(
                    playerName = "Alex Smith",
                    role = role,
                    teamName = "Senior Men",
                )
            }
        }
    }
}

@Preview(name = "EventCard match — light", showBackground = true)
@Composable
private fun EventCardMatchDarktPreview() {
    KourtTheme(darkTheme = true) {
        EventCard(
            title = "vs. Sharks",
            eventType = "match",
            dayOfWeek = "WED",
            dayOfMonth = 14,
            startTime = "19:30",
            location = "Away Game",
            modifier = Modifier.padding(16.dp),
            onClick = {},
        )
    }
}

@Preview(name = "EventCard match — light", showBackground = true)
@Composable
private fun EventCardMatchLightPreview() {
    KourtTheme(darkTheme = false) {
        EventCard(
            title = "vs. Sharks",
            eventType = "match",
            dayOfWeek = "WED",
            dayOfMonth = 14,
            startTime = "19:30",
            location = "Away Game",
            modifier = Modifier.padding(16.dp),
            onClick = {},
        )
    }
}

@Preview(name = "EventCard cancelled — dark", showBackground = true)
@Composable
private fun EventCardCancelledDarkPreview() {
    KourtTheme(darkTheme = true) {
        EventCard(
            title = "vs. Sharks",
            eventType = "match",
            status = "cancelled",
            dayOfWeek = "WED",
            dayOfMonth = 14,
            startTime = "19:30",
            location = "Away Game",
            modifier = Modifier.padding(16.dp),
            onClick = {},
        )
    }
}

@Preview(name = "EventCard past — dark", showBackground = true)
@Composable
private fun EventCardPastDarkPreview() {
    KourtTheme(darkTheme = true) {
        EventCard(
            title = "vs. Lakers",
            eventType = "match",
            status = "completed",
            dayOfWeek = "SAT",
            dayOfMonth = 5,
            startTime = "14:00",
            location = "Home Arena",
            modifier = Modifier.padding(16.dp),
            onClick = {},
        )
    }
}

@Preview(name = "EventCard practice — dark", showBackground = true)
@Composable
private fun EventCardPracticeDarkPreview() {
    KourtTheme(darkTheme = true) {
        EventCard(
            title = "Training Session",
            eventType = "practice",
            dayOfWeek = "TUE",
            dayOfMonth = 22,
            startTime = "09:00",
            location = "Training Ground",
            modifier = Modifier.padding(16.dp),
            onClick = {},
        )
    }
}

@Preview(name = "EventCard practice — Light", showBackground = true)
@Composable
private fun EventCardPracticeLightPreview() {
    KourtTheme(darkTheme = false) {
        EventCard(
            title = "Training Session",
            eventType = "practice",
            dayOfWeek = "TUE",
            dayOfMonth = 22,
            startTime = "09:00",
            location = "Training Ground",
            modifier = Modifier.padding(16.dp),
            onClick = {},
        )
    }
}
