package com.kourt.app.ui.screens.team.create

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
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
import com.kourt.app.ui.components.AuthFieldLabel
import com.kourt.app.ui.components.BaseScreen
import com.kourt.app.ui.components.CategoryChip
import com.kourt.app.ui.components.KourtButton
import com.kourt.app.ui.components.bottomSheets.TeamIdentityBottomSheet
import com.kourt.app.ui.components.authTextFieldColors
import com.kourt.app.ui.components.bottomSheets.contentColorForBackground
import com.kourt.app.ui.theme.KourtTheme
import com.kourt.app.ui.theme.White
import com.kourt.app.viewmodel.team.AddTeamViewModel
import androidx.core.graphics.toColorInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddTeamScreen(
    navigation: INavigationRouter,
    viewModel: AddTeamViewModel = hiltViewModel(),
) {
    val uiState = viewModel.uiState
    val placeholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
    val iconTintColor = MaterialTheme.colorScheme.onBackground

    val accentColor = remember(uiState.accentColor) {
        runCatching { Color(uiState.accentColor.toColorInt()) }
            .getOrDefault(Color(0xFF9CA3AF))
    }
    var showIdentitySheet by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            viewModel.onSaveConsumed()
            navigation.navigateToClubManagementScreen()
        }
    }

    if (uiState.showDeleteDialog) {
        DeleteTeamDialog(
            onDismiss = { viewModel.onDeleteTeamDismiss() },
            onConfirm = { viewModel.onDeleteTeamConfirm() },
        )
    }

    BaseScreen(
        title = stringResource(if (uiState.isEditMode) R.string.edit_team_title else R.string.add_team_title),
        onBack = { navigation.returnBack() },
        trailingIcon = if (uiState.isEditMode) {
            {
                IconButton(
                    onClick = { viewModel.onDeleteTeamClick() },
                    enabled = !uiState.isLoading,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = stringResource(R.string.add_team_delete_button),
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }
        } else null,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // ── Team avatar preview ───────────────────────────────────────────
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(accentColor)
                        .clickable { showIdentitySheet = true },
                ) {
                    Text(
                        text = uiState.initials.take(3),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = accentColor.contentColorForBackground(),
                    )

                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = stringResource(R.string.add_team_brand_label),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                )
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = stringResource(R.string.create_club_upload_logo_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                )

            }

            if (showIdentitySheet) {
                TeamIdentityBottomSheet(
                    selectedColor = accentColor,
                    selectedInitials = uiState.initials,
                    onDismiss = { showIdentitySheet = false },
                    onApply = { color, initials ->
                        viewModel.onAccentColorChanged(color)
                        viewModel.onInitialsChanged(initials)
                    },
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ── Team Name ─────────────────────────────────────────────────────
            AuthFieldLabel(label = stringResource(R.string.add_team_name_label))
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = uiState.teamName,
                onValueChange = { viewModel.onTeamNameChange(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = stringResource(R.string.add_team_name_placeholder),
                        style = MaterialTheme.typography.bodyMedium,
                        color = placeholderColor,
                    )
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.group),
                        contentDescription = null,
                        tint = iconTintColor,
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next,
                ),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = authTextFieldColors(placeholderColor = placeholderColor),
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ── Head Coach ────────────────────────────────────────────────────
            AuthFieldLabel(label = stringResource(R.string.add_team_head_coach_label))
            Spacer(modifier = Modifier.height(6.dp))
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = uiState.headCoachQuery,
                    onValueChange = { viewModel.onHeadCoachQueryChange(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = stringResource(R.string.add_team_head_coach_placeholder),
                            style = MaterialTheme.typography.bodyMedium,
                            color = placeholderColor,
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = null,
                            tint = iconTintColor,
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next,
                    ),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = authTextFieldColors(placeholderColor = placeholderColor),
                )
                DropdownMenu(
                    expanded = uiState.headCoachSuggestions.isNotEmpty(),
                    onDismissRequest = {},
                    properties = PopupProperties(focusable = false),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface),
                ) {
                    uiState.headCoachSuggestions.forEach { user ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = user.displayName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            },
                            onClick = { viewModel.onHeadCoachSelected(user) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Person,
                                    contentDescription = null,
                                    tint = iconTintColor,
                                    modifier = Modifier.size(16.dp),
                                )
                            },
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Assistant Coach(es) ───────────────────────────────────────────
            AuthFieldLabel(label = stringResource(R.string.add_team_assistant_label))
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = uiState.assistantQuery,
                    onValueChange = { viewModel.onAssistantQueryChange(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = stringResource(R.string.add_team_assistant_placeholder),
                            style = MaterialTheme.typography.bodyMedium,
                            color = placeholderColor,
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = null,
                            tint = iconTintColor,
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Done,
                    ),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = authTextFieldColors(placeholderColor = placeholderColor),
                )
                DropdownMenu(
                    expanded = uiState.assistantSuggestions.isNotEmpty(),
                    onDismissRequest = {},
                    properties = PopupProperties(focusable = false),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface),
                ) {
                    uiState.assistantSuggestions.forEach { user ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = user.displayName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            },
                            onClick = { viewModel.onAssistantSelected(user) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Person,
                                    contentDescription = null,
                                    tint = iconTintColor,
                                    modifier = Modifier.size(16.dp),
                                )
                            },
                        )
                    }
                }
                }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable { viewModel.onAssistantAdd() },
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = stringResource(R.string.add_team_add_assistant_cd),
                        tint = White,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
            // Selected assistant chips
            if (uiState.selectedAssistants.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    uiState.selectedAssistants.forEach { user ->
                        AssistantChip(
                            name = user.displayName,
                            onRemove = { viewModel.onAssistantRemoved(user) },
                            iconTintColor = iconTintColor,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Team Category ─────────────────────────────────────────────────
            AuthFieldLabel(label = stringResource(R.string.add_team_category_label))
            Spacer(modifier = Modifier.height(10.dp))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    CategoryChip(
                        label = stringResource(R.string.add_team_category_men),
                        value = "men",
                        selected = uiState.category == "men",
                        onClick = { viewModel.onCategoryChange("men") },
                        modifier = Modifier.weight(1f),
                    )
                    CategoryChip(
                        label = stringResource(R.string.add_team_category_women),
                        value = "women",
                        selected = uiState.category == "women",
                        onClick = { viewModel.onCategoryChange("women") },
                        modifier = Modifier.weight(1f),
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    CategoryChip(
                        label = stringResource(R.string.add_team_category_children),
                        value = "children",
                        selected = uiState.category == "children",
                        onClick = { viewModel.onCategoryChange("children") },
                        modifier = Modifier.weight(1f),
                    )
                    CategoryChip(
                        label = stringResource(R.string.add_team_category_seniors),
                        value = "seniors",
                        selected = uiState.category == "seniors",
                        onClick = { viewModel.onCategoryChange("seniors") },
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Location & Arena (expandable) ─────────────────────────────────
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
            ) {
                Column {
                    // Header row — always visible
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.onToggleLocation() }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.city),
                            contentDescription = null,
                            tint = iconTintColor,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(R.string.add_team_location_section),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f),
                        )
                        Icon(
                            imageVector = if (uiState.isLocationExpanded)
                                Icons.Filled.KeyboardArrowUp
                            else
                                Icons.Filled.KeyboardArrowDown,
                            contentDescription = null,
                            tint = iconTintColor,
                        )
                    }

                    // Expandable fields
                    AnimatedVisibility(
                        visible = uiState.isLocationExpanded,
                        enter = expandVertically(),
                        exit = shrinkVertically(),
                    ) {
                        Column(
                            modifier = Modifier.padding(
                                start = 16.dp,
                                end = 16.dp,
                                bottom = 16.dp,
                            ),
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            OutlinedTextField(
                                value = uiState.location,
                                onValueChange = { viewModel.onLocationChange(it) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = placeholderColor,
                                    unfocusedLabelColor = placeholderColor,
                                ),
                                label = {
                                    Text(
                                        text = stringResource(R.string.add_team_location_label),
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Text,
                                    capitalization = KeyboardCapitalization.Words,
                                    imeAction = ImeAction.Next,
                                ),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                            OutlinedTextField(
                                value = uiState.arena,
                                onValueChange = { viewModel.onArenaChange(it) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = placeholderColor,
                                    unfocusedLabelColor = placeholderColor,
                                ),
                                label = {
                                    Text(
                                        text = stringResource(R.string.add_team_arena_label),
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Text,
                                    capitalization = KeyboardCapitalization.Words,
                                    imeAction = ImeAction.Done,
                                ),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ── Error ─────────────────────────────────────────────────────────
            if (uiState.error != null) {
                Text(
                    text = stringResource(uiState.error),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                )
            }

            // ── Save / Delete buttons ─────────────────────────────────────────
            KourtButton(
                text = stringResource(if (uiState.isEditMode) R.string.add_team_save_changes_button else R.string.add_team_save_button),
                onClick = { viewModel.onSaveTeam() },
                enabled = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

// ── Private composables ───────────────────────────────────────────────────────

@Composable
private fun DeleteTeamDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.delete_team_title),
                style = MaterialTheme.typography.titleLarge,
            )
        },
        text = {
            Text(
                text = stringResource(R.string.delete_team_message),
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.action_cancel),
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError,
                ),
            ) {
                Text(text = stringResource(R.string.delete_team_confirm))
            }
        },
    )
}

@Preview(name = "Delete Team Dialog — Light", showBackground = true)
@Composable
private fun DeleteTeamDialogLightPreview() {
    KourtTheme(darkTheme = false) {
        DeleteTeamDialog(
            onDismiss = {},
            onConfirm = {},
        )
    }
}

@Preview(name = "Delete Team Dialog — Dark", showBackground = true, backgroundColor = 0xFF131212)
@Composable
private fun DeleteTeamDialogDarkPreview() {
    KourtTheme(darkTheme = true) {
        DeleteTeamDialog(
            onDismiss = {},
            onConfirm = {},
        )
    }
}

@Composable
private fun AssistantChip(
    name: String,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
    iconTintColor: Color,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(start = 12.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        IconButton(
            onClick = onRemove,
            modifier = Modifier.size(18.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(R.string.add_team_remove_assistant_cd, name),
                tint = iconTintColor,
                modifier = Modifier.size(12.dp),
            )
        }
    }
}
