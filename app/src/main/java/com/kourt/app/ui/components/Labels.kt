package com.kourt.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import com.kourt.app.R
import com.kourt.app.ui.screens.club.management.ClubMemberUiItem
import com.kourt.app.ui.screens.dashboard.coach.EventUiItem
import java.util.Locale

// Display text for values stored in Firestore. Data keeps the raw keys ("player", "match",
// "out_of_town", …) and the UI translates them here, so they follow the app language.
// Unknown values are shown as stored rather than hidden.

/** Display name for a member role ("player", "coach", "assistant", "parent", "admin"). */
@Composable
fun roleLabel(role: String): String = when (role.lowercase()) {
    "player" -> stringResource(R.string.members_filter_player)
    "coach" -> stringResource(R.string.members_filter_coach)
    "assistant" -> stringResource(R.string.members_filter_assistant)
    "parent" -> stringResource(R.string.members_filter_parent)
    "admin" -> stringResource(R.string.role_admin)
    else -> role
}

/** Display name for an event type ("match", "practice"). */
@Composable
fun eventTypeLabel(type: String): String = when (type.lowercase()) {
    "match" -> stringResource(R.string.event_type_match)
    "practice" -> stringResource(R.string.event_type_practice)
    else -> type
}

/** "Today", "Tomorrow", or the event's formatted date. */
@Composable
fun eventDateLabel(event: EventUiItem): String = when {
    event.isToday -> stringResource(R.string.date_today)
    event.isTomorrow -> stringResource(R.string.date_tomorrow)
    else -> event.dateLabel
}

/** Display text for an RSVP reason key ("sick", "work", "out_of_town", "other"); blank means none given. */
@Composable
fun rsvpReasonLabel(reason: String): String = when (reason) {
    "" -> stringResource(R.string.rsvp_reason_none)
    "sick" -> stringResource(R.string.rsvp_reason_sick)
    "work" -> stringResource(R.string.rsvp_reason_work)
    "out_of_town" -> stringResource(R.string.rsvp_reason_out_of_town)
    "other" -> stringResource(R.string.rsvp_reason_other)
    else -> reason
}

/** Line under a member's name: their child for parents ("Guardian of: …"), otherwise their teams. */
@Composable
fun memberSubtitle(member: ClubMemberUiItem): String = when {
    member.childName != null -> stringResource(R.string.member_subtitle_guardian, member.childName)
    member.teamNames.isNotBlank() -> stringResource(R.string.member_subtitle_team, member.teamNames)
    else -> ""
}

/**
 * Country name in the app language for an ISO code ("CZ" → "Česko" / "Czechia").
 * Clubs saved before codes were used store an English name, which is shown as it is.
 */
@Composable
fun countryName(code: String): String {
    if (code.length != 2) return code
    val appLocale = LocalConfiguration.current.locales[0]
    val region = runCatching { Locale.Builder().setRegion(code).build() }.getOrNull() ?: return code
    return region.getDisplayCountry(appLocale).ifBlank { code }
}
