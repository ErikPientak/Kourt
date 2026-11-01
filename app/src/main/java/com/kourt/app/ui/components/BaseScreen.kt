package com.kourt.app.ui.components

import androidx.annotation.StringRes
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
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kourt.app.R
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
 * @param snackbarHostState Where [ErrorSnackbarEffect] shows messages; `null` for none.
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
    snackbarHostState: SnackbarHostState? = null,
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
                            contentDescription = stringResource(R.string.common_back_cd),
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
        snackbarHost = { snackbarHostState?.let { SnackbarHost(it) } },
        content = content,
    )
}

/**
 * Shows [error] once in [snackbarHostState], then calls [onShown] so the screen can clear it.
 * With [onRetry] the snackbar gets a Retry action, for loads that failed.
 */
@Composable
fun ErrorSnackbarEffect(
    snackbarHostState: SnackbarHostState,
    @StringRes error: Int?,
    onShown: () -> Unit,
    onRetry: (() -> Unit)? = null,
) {
    val message = error?.let { stringResource(it) }
    val retryLabel = stringResource(R.string.members_error_retry)
    val currentOnShown by rememberUpdatedState(onShown)
    val currentOnRetry by rememberUpdatedState(onRetry)

    LaunchedEffect(message) {
        if (message == null) return@LaunchedEffect
        val result = snackbarHostState.showSnackbar(
            message = message,
            actionLabel = if (currentOnRetry != null) retryLabel else null,
            duration = if (currentOnRetry != null) SnackbarDuration.Long else SnackbarDuration.Short,
        )
        // Cleared only after it's been shown: clearing first would restart this effect
        // and cancel the snackbar straight away.
        currentOnShown()
        if (result == SnackbarResult.ActionPerformed) currentOnRetry?.invoke()
    }
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
