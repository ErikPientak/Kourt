package com.kourt.app.ui.components

import androidx.compose.ui.graphics.Color
import com.kourt.app.ui.theme.RoleBadgeAdmin
import com.kourt.app.ui.theme.RoleBadgeAssistant
import com.kourt.app.ui.theme.RoleBadgeCaptain
import com.kourt.app.ui.theme.RoleBadgeCoach
import com.kourt.app.ui.theme.RoleBadgeParent
import com.kourt.app.ui.theme.RoleBadgePlayer

fun roleBadgeColor(role: String): Color = when (role.lowercase()) {
    "player"    -> RoleBadgePlayer
    "coach"     -> RoleBadgeCoach
    "captain"   -> RoleBadgeCaptain
    "assistant" -> RoleBadgeAssistant
    "parent"    -> RoleBadgeParent
    "admin"    -> RoleBadgeAdmin
    else        -> RoleBadgePlayer
}