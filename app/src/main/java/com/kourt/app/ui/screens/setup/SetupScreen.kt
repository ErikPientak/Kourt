package com.kourt.app.ui.screens.setup

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.kourt.app.navigation.Destination
import com.kourt.app.navigation.INavigationRouter
import com.kourt.app.ui.components.KourtButton
import com.kourt.app.ui.components.SetupOptionCard
import com.kourt.app.ui.theme.White
import com.kourt.app.viewmodel.setup.SetupViewModel

@Composable
fun SetupScreen(
    navigation: INavigationRouter,
    viewModel: SetupViewModel = hiltViewModel(),
) {
    val uiState = viewModel.uiState

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            viewModel.onSaveConsumed()
            navigation.navigateToPlayerDashboard()
        }
    }

    val previousRoute = navigation.getNavController().previousBackStackEntry?.destination?.route
    val showBack = previousRoute == Destination.ProfileScreen.route

    SetupScreenContent(
        uiState = uiState,
        actions = viewModel,
        onNavigateToCreateClub = { navigation.navigateToCreateClubScreen() },
        onNavigateToAddChild = { navigation.navigateToAddChild() },
        onBack = if (showBack) navigation::returnBack else null,
    )
}

@Composable
internal fun SetupScreenContent(
    uiState: SetupScreenUiState,
    actions: SetupScreenActions,
    onNavigateToCreateClub: () -> Unit,
    onNavigateToAddChild: () -> Unit = {},
    onBack: (() -> Unit)? = null,
) {
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

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Navigate back",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
            Text(
                text = stringResource(R.string.setup_title),
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }

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
                onClick = onNavigateToCreateClub,
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
                onValueChange = { actions.onJoinCodeChange(it) },
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
                onClick = { actions.onJoinTeam() },
                enabled = !uiState.isLoading,
                modifier = Modifier
                    .padding(start = 68.dp)
                    .fillMaxWidth(),
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Parent card
        SetupOptionCard(
            icon = painterResource(R.drawable.group),
            iconContentDescription = stringResource(R.string.setup_parent_title),
            title = stringResource(R.string.setup_parent_title),
            subtitle = stringResource(R.string.setup_parent_subtitle),
            modifier = Modifier.fillMaxWidth(),
        ) {
            KourtButton(
                text = stringResource(R.string.setup_parent_button),
                onClick = onNavigateToAddChild,
                modifier = Modifier
                    .padding(start = 68.dp)
                    .fillMaxWidth(),
            )
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}
