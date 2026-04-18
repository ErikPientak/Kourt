package com.kourt.app.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kourt.app.R
import com.kourt.app.navigation.INavigationRouter
import com.kourt.app.ui.components.BaseScreen
import com.kourt.app.ui.theme.KourtTheme
import com.kourt.app.viewmodel.settings.SettingsViewModel

@Composable
fun SettingsScreen(
    navigation: INavigationRouter,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState = viewModel.uiState

    LaunchedEffect(uiState.isLoggedOut) {
        if (uiState.isLoggedOut) {
            viewModel.onSuccessConsumed()
            navigation.navigateToLoginScreen()
        }
    }

    SettingsScreenContent(
        uiState = uiState,
        isDarkTheme = isDarkTheme,
        onBack = navigation::returnBack,
        onToggleTheme = onToggleTheme,
        onProfileClick = navigation::navigateToProfileScreen,
        onLanguageClick = { /* TODO: navigate to Language picker screen */ },
        onLogOut = viewModel::onLogOut,
        onDeleteAccountClick = viewModel::onDeleteAccountClick,
        onConfirmDeleteAccount = viewModel::onConfirmDeleteAccount,
        onDismissDeleteDialog = viewModel::onDismissDeleteDialog,
    )
}

@Composable
private fun SettingsScreenContent(
    uiState: SettingsScreenUiState,
    isDarkTheme: Boolean,
    onBack: () -> Unit,
    onToggleTheme: () -> Unit,
    onProfileClick: () -> Unit,
    onLanguageClick: () -> Unit,
    onLogOut: () -> Unit,
    onDeleteAccountClick: () -> Unit,
    onConfirmDeleteAccount: () -> Unit,
    onDismissDeleteDialog: () -> Unit,
) {
    if (uiState.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = onDismissDeleteDialog,
            title = { Text(stringResource(R.string.settings_delete_account_title)) },
            text = { Text(stringResource(R.string.settings_delete_account_message)) },
            confirmButton = {
                TextButton(onClick = onConfirmDeleteAccount) {
                    Text(
                        text = stringResource(R.string.settings_delete_account_confirm),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissDeleteDialog) {
                    Text(
                        text = stringResource(R.string.action_cancel),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            },
        )
    }

    val context = LocalContext.current
    val versionName = remember {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0"
    }

    BaseScreen(
        title = stringResource(R.string.settings_title),
        onBack = onBack,
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary,
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    // Profile
                    SettingsTileChevron(
                        icon = painterResource(R.drawable.admin),
                        label = stringResource(R.string.settings_profile),
                        onClick = onProfileClick,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // App Theme
                    SettingsTileToggle(
                        icon = painterResource(R.drawable.dark_mode),
                        label = stringResource(R.string.settings_app_theme),
                        subtitle = stringResource(
                            if (isDarkTheme) R.string.settings_dark_mode_on
                            else R.string.settings_dark_mode_off
                        ),
                        checked = isDarkTheme,
                        onCheckedChange = { onToggleTheme() },
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Language
                    SettingsTileChevron(
                        icon = painterResource(R.drawable.globe),
                        label = stringResource(R.string.settings_language),
                        onClick = onLanguageClick,
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    // Log Out button
                    LogOutButton(onClick = onLogOut)

                    Spacer(modifier = Modifier.height(12.dp))

                    // Delete Account
                    TextButton(
                        onClick = onDeleteAccountClick,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = stringResource(R.string.settings_delete_account),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Version
                    Text(
                        text = stringResource(R.string.settings_version, versionName),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

// ── Tile components ───────────────────────────────────────────────────────────

@Composable
private fun SettingsTileChevron(
    icon: Painter,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SettingsTileBase(
        icon = icon,
        label = label,
        subtitle = null,
        modifier = modifier.clickable(onClick = onClick),
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        )
    }
}

@Composable
private fun SettingsTileToggle(
    icon: Painter,
    label: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    SettingsTileBase(
        icon = icon,
        label = label,
        subtitle = subtitle,
        modifier = modifier,
    ) {
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                uncheckedBorderColor = Color.Transparent,
            ),
        )
    }
}

@Composable
private fun SettingsTileBase(
    icon: Painter,
    label: String,
    subtitle: String?,
    modifier: Modifier = Modifier,
    trailing: @Composable () -> Unit,
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val isDark = MaterialTheme.colorScheme.background.blue < 0.5f
    val iconContainerColor = if (isDark) primaryColor.copy(alpha = 0.15f) else Color.Transparent
    val iconTintColor = MaterialTheme.colorScheme.onBackground

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(52.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Icon circle — visible in dark mode only
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(iconContainerColor, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = icon,
                    contentDescription = null,
                    tint = iconTintColor,
                    modifier = Modifier.size(22.dp),
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (subtitle != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                    )
                }
            }

            trailing()
        }
    }
}

@Composable
private fun LogOutButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val primaryColor = MaterialTheme.colorScheme.primary

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.height(80.dp),
    ) {
        androidx.compose.material3.Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(50.dp),
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                containerColor = primaryColor,
                contentColor = Color.White,
            ),
            elevation = androidx.compose.material3.ButtonDefaults.buttonElevation(
                defaultElevation = 0.dp,
                pressedElevation = 0.dp,
            ),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.settings_log_out),
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(name = "SettingsScreen — dark", showBackground = true)
@Composable
private fun SettingsScreenDarkPreview() {
    KourtTheme(darkTheme = true) {
        SettingsScreenContent(
            uiState = SettingsScreenUiState(),
            isDarkTheme = true,
            onBack = {},
            onToggleTheme = {},
            onProfileClick = {},
            onLanguageClick = {},
            onLogOut = {},
            onDeleteAccountClick = {},
            onConfirmDeleteAccount = {},
            onDismissDeleteDialog = {},
        )
    }
}

@Preview(name = "SettingsScreen — light", showBackground = true)
@Composable
private fun SettingsScreenLightPreview() {
    KourtTheme(darkTheme = false) {
        SettingsScreenContent(
            uiState = SettingsScreenUiState(),
            isDarkTheme = false,
            onBack = {},
            onToggleTheme = {},
            onProfileClick = {},
            onLanguageClick = {},
            onLogOut = {},
            onDeleteAccountClick = {},
            onConfirmDeleteAccount = {},
            onDismissDeleteDialog = {},
        )
    }
}

@Preview(name = "SettingsScreen — delete dialog", showBackground = true)
@Composable
private fun SettingsScreenDeleteDialogPreview() {
    KourtTheme(darkTheme = true) {
        SettingsScreenContent(
            uiState = SettingsScreenUiState(showDeleteDialog = true),
            isDarkTheme = true,
            onBack = {},
            onToggleTheme = {},
            onProfileClick = {},
            onLanguageClick = {},
            onLogOut = {},
            onDeleteAccountClick = {},
            onConfirmDeleteAccount = {},
            onDismissDeleteDialog = {},
        )
    }
}
