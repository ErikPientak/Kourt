package com.kourt.app.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kourt.app.ui.theme.KourtTheme

/**
 * Reusable screen shell for root-level Kourt screens that need a standard top
 * bar **and** a bottom navigation bar — i.e. screens that have no back arrow.
 *
 * Wraps [Scaffold] + [CenterAlignedTopAppBar] with the same bottom-border
 * drawing and color overrides as [BaseScreen], but adds [bottomBar] and
 * [floatingActionButton] slots so callers don't have to manage insets or FAB
 * positioning manually.
 *
 * @param title Text displayed in the centre of the top bar.
 * @param modifier Applied to the outermost [Scaffold].
 * @param trailingIcon Optional slot for a trailing action (e.g. settings gear).
 *   Pass `null` (default) to omit entirely — no space is reserved.
 * @param bottomBar Slot for the bottom navigation component.
 * @param floatingActionButton Slot for an optional FAB. [Scaffold] positions it
 *   automatically above the bottom bar — do not add manual bottom padding.
 *   Defaults to an empty composable (no FAB).
 * @param content Slot for the screen body. Receives [PaddingValues] from
 *   [Scaffold] that the caller must apply to avoid content being obscured by
 *   the bars or system insets.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BaseScreenWithBottomNav(
    title: String,
    modifier: Modifier = Modifier,
    trailingIcon: (@Composable () -> Unit)? = null,
    bottomBar: @Composable () -> Unit,
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    val outlineColor = MaterialTheme.colorScheme.surface

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
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
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    )
                },
                actions = {
                    if (trailingIcon != null) {
                        trailingIcon()
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = MaterialTheme.colorScheme.primary,
                ),
            )
        },
        bottomBar = bottomBar,
        floatingActionButton = floatingActionButton,
        content = content,
    )
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(name = "BaseScreenWithBottomNav — dark", showBackground = true)
@Composable
private fun BaseScreenWithBottomNavDarkPreview() {
    KourtTheme(darkTheme = true) {
        BaseScreenWithBottomNav(
            title = "Club Management",
            trailingIcon = {
                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                    )
                }
            },
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        selected = true,
                        onClick = {},
                        icon = {},
                        label = { Text("Teams") },
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = {},
                        icon = {},
                        label = { Text("Members") },
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = {},
                        icon = {},
                        label = { Text("Events") },
                    )
                }
            },
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize())
        }
    }
}

@Preview(name = "BaseScreenWithBottomNav — light", showBackground = true)
@Composable
private fun BaseScreenWithBottomNavLightPreview() {
    KourtTheme(darkTheme = false) {
        BaseScreenWithBottomNav(
            title = "Club Management",
            trailingIcon = {
                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                    )
                }
            },
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        selected = true,
                        onClick = {},
                        icon = {},
                        label = { Text("Teams") },
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = {},
                        icon = {},
                        label = { Text("Members") },
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = {},
                        icon = {},
                        label = { Text("Events") },
                    )
                }
            },
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize())
        }
    }
}
