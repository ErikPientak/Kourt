package com.kourt.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.kourt.app.R

private val RsvpGoingColor = Color(0xFF22C55E)
private val RsvpLateColor = Color(0xFFF97316)
private val RsvpNotGoingColor = Color(0xFFEF4444)

/**
 * Label and color for an RSVP status as stored in Firestore ("yes", "late", "no"),
 * or `null` for an unknown status.
 */
@Composable
fun rsvpStatusLabelAndColor(status: String): Pair<String, Color>? = when (status.lowercase()) {
    "yes"  -> stringResource(R.string.player_dashboard_rsvp_going) to RsvpGoingColor
    "late" -> stringResource(R.string.player_dashboard_rsvp_late) to RsvpLateColor
    "no"   -> stringResource(R.string.player_dashboard_rsvp_not_going) to RsvpNotGoingColor
    else   -> null
}
