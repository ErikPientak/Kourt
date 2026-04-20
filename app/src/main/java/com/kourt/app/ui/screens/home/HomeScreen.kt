package com.kourt.app.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kourt.app.R
import com.kourt.app.navigation.INavigationRouter
import com.kourt.app.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    navigation: INavigationRouter,
    isDarkTheme: Boolean = true,
    currentLanguage: String = "en",
    onToggleTheme: () -> Unit = {},
    onSetLanguage: (String) -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel(),
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // ── Theme toggle ──────────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.home_theme_label),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f),
            )
            OutlinedButton(
                onClick = onToggleTheme,
                modifier = Modifier.weight(1f),
            ) {
                Icon(
                    painter = if (isDarkTheme) painterResource(R.drawable.dark_mode)  else painterResource(R.drawable.light_mode),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = if (isDarkTheme) stringResource(R.string.home_theme_light) else stringResource(R.string.home_theme_dark),
                    modifier = Modifier.padding(start = 6.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ── Language toggle ───────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.home_language_label),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f),
            )
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                val isEn = currentLanguage == "en"
                Button(
                    onClick = { if (!isEn) onSetLanguage("en") },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isEn) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surface,
                        contentColor = if (isEn) MaterialTheme.colorScheme.onPrimary
                                       else MaterialTheme.colorScheme.onSurface,
                    ),
                ) {
                    Text("EN")
                }
                Button(
                    onClick = { if (isEn) onSetLanguage("cs") },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (!isEn) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surface,
                        contentColor = if (!isEn) MaterialTheme.colorScheme.onPrimary
                                       else MaterialTheme.colorScheme.onSurface,
                    ),
                ) {
                    Text("CZ")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        Spacer(modifier = Modifier.height(16.dp))

        // ── Dev navigation ────────────────────────────────────────────────────
        Button(
            onClick = { navigation.navigateToLoginScreen() },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = stringResource(R.string.home_go_login),
                style = MaterialTheme.typography.labelLarge,
            )
        }

        Button(
            onClick = { navigation.navigateToRegisterScreen() },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = stringResource(R.string.home_go_register),
                style = MaterialTheme.typography.labelLarge,
            )
        }

        Button(
            onClick = { navigation.navigateToSetupScreen() },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = stringResource(R.string.home_go_setup),
                style = MaterialTheme.typography.labelLarge,
            )
        }

        Button(
            onClick = { navigation.navigateToClubManagementScreen() },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = stringResource(R.string.home_go_club_management),
                style = MaterialTheme.typography.labelLarge,
            )
        }

        Button(
            onClick = { navigation.navigateToSettingsScreen() },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = "Go to Settings",
                style = MaterialTheme.typography.labelLarge,
            )
        }

        Button(
            onClick = { navigation.navigateToDashboard() },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = "Go to DashBoard",
                style = MaterialTheme.typography.labelLarge,
            )
        }

        Button(
            onClick = {
                viewModel.deleteUser {
                    navigation.navigateToLoginScreen()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error,
            ),
        ) {
            Text(
                text = stringResource(R.string.home_delete_user),
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}
