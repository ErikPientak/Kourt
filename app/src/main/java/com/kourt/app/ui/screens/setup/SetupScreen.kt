package com.kourt.app.ui.screens.setup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kourt.app.R
import com.kourt.app.navigation.INavigationRouter
import com.kourt.app.ui.components.KourtButton
import com.kourt.app.ui.components.SetupOptionCard
import com.kourt.app.ui.theme.White
import com.kourt.app.viewmodel.SetupViewModel

@Composable
fun SetupScreen(
    navigation: INavigationRouter,
    viewModel: SetupViewModel = hiltViewModel(),
) {
    val uiState = viewModel.uiState

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            navigation.navigateToSettingsScreen()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = stringResource(R.string.setup_greeting),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = stringResource(R.string.setup_title),
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.primary,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.setup_subtitle),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(R.string.setup_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Create Club card
        SetupOptionCard(
            icon = painterResource(R.drawable.whistle),
            iconContentDescription = stringResource(R.string.setup_create_club_title),
            title = stringResource(R.string.setup_create_club_title),
            subtitle = stringResource(R.string.setup_create_club_subtitle),
            modifier = Modifier.fillMaxWidth(),
        ) {
            KourtButton(
                text = stringResource(R.string.setup_create_club_button),
                onClick = { navigation.navigateToCreateClubScreen() },
                modifier = Modifier
                    .padding(start = 68.dp)
                    .fillMaxWidth(),
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Join a Team card
        SetupOptionCard(
            icon = painterResource(R.drawable.handshake),
            iconContentDescription = stringResource(R.string.setup_join_team_title),
            title = stringResource(R.string.setup_join_team_title),
            subtitle = stringResource(R.string.setup_join_team_subtitle),
            modifier = Modifier.fillMaxWidth(),
        ) {
            OutlinedTextField(
                value = uiState.joinCode,
                onValueChange = { viewModel.onJoinCodeChange(it) },
                modifier = Modifier
                    .padding(start = 68.dp)
                    .fillMaxWidth()
                    .height(52.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = White,
                    unfocusedContainerColor = White,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedBorderColor = if (uiState.error != null) MaterialTheme.colorScheme.error else Color.Transparent,
                    unfocusedBorderColor = if (uiState.error != null) MaterialTheme.colorScheme.error else Color.Transparent,
                ),
                placeholder = {
                    Text(
                        text = stringResource(R.string.setup_join_code_placeholder),
                        style = MaterialTheme.typography.labelLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                        color = Color.Black.copy(alpha = 0.4f),
                    )
                },
                textStyle = MaterialTheme.typography.labelLarge.copy(
                    textAlign = TextAlign.Center,
                    color = Color.Black,
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    capitalization = KeyboardCapitalization.Characters,
                    imeAction = ImeAction.Done,
                ),
                singleLine = true,
                shape = RoundedCornerShape(50.dp),
                enabled = !uiState.isLoading,
            )

            if (uiState.error != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(uiState.error),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(start = 68.dp),
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            KourtButton(
                text = stringResource(R.string.setup_join_button),
                onClick = { viewModel.onJoinTeam() },
                enabled = !uiState.isLoading,
                modifier = Modifier
                    .padding(start = 68.dp)
                    .fillMaxWidth(),
            )
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}
