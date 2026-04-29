package com.kourt.app.ui.screens.setup.addchild

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.kourt.app.ui.components.BaseScreen
import com.kourt.app.ui.components.KourtButton
import com.kourt.app.ui.components.bottomSheets.AvatarPickerBottomSheet
import com.kourt.app.ui.components.bottomSheets.avatarResId
import com.kourt.app.ui.theme.White
import com.kourt.app.viewmodel.setup.AddChildViewModel

@Composable
fun AddChildScreen(
    navigation: INavigationRouter,
    viewModel: AddChildViewModel = hiltViewModel(),
) {
    val uiState = viewModel.uiState

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            viewModel.onSuccessConsumed()
            navigation.navigateToPlayerDashboard()
        }
    }

    AddChildScreenContent(
        uiState = uiState,
        actions = viewModel,
        onBack = navigation::returnBack,
    )
}

@Composable
private fun AddChildScreenContent(
    uiState: AddChildScreenUiState,
    actions: AddChildScreenActions,
    onBack: () -> Unit,
) {
    if (uiState.showAvatarPicker) {
        AvatarPickerBottomSheet(
            currentAvatarId = uiState.avatarId,
            onAvatarSelected = actions::onAvatarSelected,
            onDismiss = actions::onAvatarPickerDismiss,
        )
    }

    if (uiState.showJoinTeamDialog) {
        JoinTeamDialog(
            uiState = uiState,
            actions = actions,
        )
    }

    BaseScreen(
        title = stringResource(R.string.add_child_title),
        onBack = onBack,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            // Avatar picker
            Box(
                contentAlignment = Alignment.BottomEnd,
                modifier = Modifier.size(100.dp),
            ) {
                val resId = avatarResId(uiState.avatarId)
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface),
                ) {
                    if (resId != null) {
                        Image(
                            painter = painterResource(resId),
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                        )
                    } else {
                        Icon(
                            painter = painterResource(R.drawable.person_search),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            modifier = Modifier.size(52.dp),
                        )
                    }
                }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable(onClick = actions::onAvatarPickerShow),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.person_edit),
                        contentDescription = stringResource(R.string.add_child_avatar_label),
                        tint = Color.White,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = stringResource(R.string.add_child_name_label),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onBackground,
                )
                OutlinedTextField(
                    value = uiState.displayName,
                    onValueChange = actions::onDisplayNameChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = stringResource(R.string.add_child_name_placeholder),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        )
                    },
                    isError = uiState.profileError != null,
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Done,
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                    ),
                    enabled = !uiState.isLoading,
                )
                if (uiState.profileError != null) {
                    Text(
                        text = stringResource(uiState.profileError),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            KourtButton(
                text = stringResource(R.string.add_child_submit_button),
                onClick = actions::onSubmit,
                enabled = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

// ── Join team dialog ───────────────────────────────────────────────────────────

@Composable
private fun JoinTeamDialog(
    uiState: AddChildScreenUiState,
    actions: AddChildScreenActions,
) {
    AlertDialog(
        onDismissRequest = actions::onDismissJoinTeamDialog,
        title = {
            Text(
                text = stringResource(R.string.add_child_step2_title),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = stringResource(R.string.add_child_step2_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                )
                OutlinedTextField(
                    value = uiState.joinCode,
                    onValueChange = actions::onJoinCodeChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = White,
                        unfocusedContainerColor = White,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedBorderColor = if (uiState.joinError != null) MaterialTheme.colorScheme.error else Color.Transparent,
                        unfocusedBorderColor = if (uiState.joinError != null) MaterialTheme.colorScheme.error else Color.Transparent,
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
                if (uiState.joinError != null) {
                    Text(
                        text = stringResource(uiState.joinError),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        },
        confirmButton = {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary,
                )
            } else {
                TextButton(onClick = actions::onJoinTeam) {
                    Text(stringResource(R.string.setup_join_button))
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = actions::onDismissJoinTeamDialog,
                enabled = !uiState.isLoading,
            ) {
                Text(
                    text = stringResource(R.string.add_child_skip_button),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                )
            }
        },
        shape = RoundedCornerShape(24.dp),
    )
}
