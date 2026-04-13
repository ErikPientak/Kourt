package com.kourt.app.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kourt.app.ui.theme.KourtTheme

/**
 * Reusable screen shell for all Kourt screens that need a standard top bar.
 *
 * Uses [Scaffold] + [CenterAlignedTopAppBar] for correct edge-to-edge
 * [WindowInsets] handling, scroll behaviour support, and Material 3 compliance.
 *
 * @param title Text displayed in the centre of the top bar.
 * @param onBack Invoked when the leading back-arrow [IconButton] is tapped.
 * @param modifier Applied to the outermost [Scaffold].
 * @param trailingIcon Optional slot for a trailing action (e.g. settings gear, kebab
 *   menu). The caller provides the full [IconButton] so the click handler and icon
 *   remain flexible. Pass `null` (default) to omit entirely — no space is reserved.
 * @param content Slot for the screen body. Receives [PaddingValues] from [Scaffold]
 *   that the caller must apply (e.g. via [Modifier.padding]) to avoid content being
 *   obscured by the top bar or system bars.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BaseScreen(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    trailingIcon: (@Composable () -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit,
) {
    val outlineColor = MaterialTheme.colorScheme.surface

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                modifier = Modifier.drawWithContent {
                    drawContent() // This draws the actual TopAppBar first

                    val thickness = 1.dp.toPx() // Use 2dp to make it very obvious
                    val y = size.height - (thickness / 2)

                    drawLine(
                        color = outlineColor, // Your theme color
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = thickness
                    )
                },
                title = {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate back",
                        )
                    }
                },
                actions = {
                    if (trailingIcon != null) {
                        trailingIcon()
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = MaterialTheme.colorScheme.onBackground,
                )
            )
        },
        content = content,
    )
}

// ── Previews ─────────────────────────────────────────────────────────────────

@Preview(name = "BaseScreen — no trailing icon (dark)", showBackground = true)
@Composable
private fun BaseScreenNoTrailingIconDarkPreview() {
    KourtTheme(darkTheme = true) {
        BaseScreen(
            title = "Create Club",
            onBack = {},
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize())
        }
    }
}

@Preview(name = "BaseScreen — with trailing icon (dark)", showBackground = true)
@Composable
private fun BaseScreenWithTrailingIconDarkPreview() {
    KourtTheme(darkTheme = true) {
        BaseScreen(
            title = "Club Settings",
            onBack = {},
            trailingIcon = {
                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                    )
                }
            },
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize())
        }
    }
}

@Preview(name = "BaseScreen — no trailing icon (light)", showBackground = true)
@Composable
private fun BaseScreenNoTrailingIconLightPreview() {
    KourtTheme(darkTheme = false) {
        BaseScreen(
            title = "Create Club",
            onBack = {},
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize())
        }
    }
}

@Preview(name = "BaseScreen — with trailing icon (light)", showBackground = true)
@Composable
private fun BaseScreenWithTrailingIconLightPreview() {
    KourtTheme(darkTheme = false) {
        BaseScreen(
            title = "Club Settings",
            onBack = {},
            trailingIcon = {
                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                    )
                }
            },
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize())
        }
    }
}
