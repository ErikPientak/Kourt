package com.kourt.app.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.kourt.app.ui.theme.KourtTheme

/**
 * Reusable screen shell for root-level Kourt screens that need a top bar and a
 * bottom navigation bar. Callers own the [topBar] slot entirely — use any
 * composable (e.g. [CenterAlignedTopAppBar], a custom [Surface] row, etc.).
 *
 * @param topBar Slot for the top app bar. Receives no padding — the caller is
 *   responsible for applying `statusBarsPadding()` if needed.
 * @param modifier Applied to the outermost [Scaffold].
 * @param bottomBar Slot for the bottom navigation component.
 * @param floatingActionButton Slot for an optional FAB. [Scaffold] positions it
 *   automatically above the bottom bar. Defaults to an empty composable.
 * @param content Slot for the screen body. Receives [PaddingValues] from
 *   [Scaffold] that the caller must apply to avoid content being obscured.
 */
@Composable
fun BaseScreenWithBottomNav(
    topBar: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    bottomBar: @Composable () -> Unit,
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0),
        topBar = topBar,
        bottomBar = bottomBar,
        floatingActionButton = floatingActionButton,
        content = content,
    )
}

// ── Previews ──────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "BaseScreenWithBottomNav — dark", showBackground = true)
@Composable
private fun BaseScreenWithBottomNavDarkPreview() {
    KourtTheme(darkTheme = true) {
        BaseScreenWithBottomNav(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "Club Management",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        )
                    },
                    actions = {
                        IconButton(onClick = {}) {
                            Icon(imageVector = Icons.Default.Settings, contentDescription = null)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                    ),
                )
            },
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(selected = true, onClick = {}, icon = {}, label = { Text("Teams") })
                    NavigationBarItem(selected = false, onClick = {}, icon = {}, label = { Text("Members") })
                    NavigationBarItem(selected = false, onClick = {}, icon = {}, label = { Text("Events") })
                }
            },
        ) { Box(modifier = Modifier.fillMaxSize()) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "BaseScreenWithBottomNav — light", showBackground = true)
@Composable
private fun BaseScreenWithBottomNavLightPreview() {
    KourtTheme(darkTheme = false) {
        BaseScreenWithBottomNav(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "Club Management",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        )
                    },
                    actions = {
                        IconButton(onClick = {}) {
                            Icon(imageVector = Icons.Default.Settings, contentDescription = null)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                    ),
                )
            },
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(selected = true, onClick = {}, icon = {}, label = { Text("Teams") })
                    NavigationBarItem(selected = false, onClick = {}, icon = {}, label = { Text("Members") })
                    NavigationBarItem(selected = false, onClick = {}, icon = {}, label = { Text("Events") })
                }
            },
        ) { Box(modifier = Modifier.fillMaxSize()) }
    }
}
