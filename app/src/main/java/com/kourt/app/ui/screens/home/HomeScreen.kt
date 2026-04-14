package com.kourt.app.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kourt.app.R
import com.kourt.app.navigation.INavigationRouter
import com.kourt.app.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    navigation: INavigationRouter,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
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
