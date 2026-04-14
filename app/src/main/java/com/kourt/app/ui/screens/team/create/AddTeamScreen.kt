package com.kourt.app.ui.screens.team.create

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kourt.app.R
import com.kourt.app.data.model.User
import com.kourt.app.navigation.INavigationRouter
import com.kourt.app.ui.components.AuthFieldLabel
import com.kourt.app.ui.components.BaseScreen
import com.kourt.app.ui.components.CategoryChip
import com.kourt.app.ui.components.KourtButton
import com.kourt.app.ui.components.authTextFieldColors
import com.kourt.app.ui.theme.DarkPlaceholder
import com.kourt.app.ui.theme.LightPlaceholder
import com.kourt.app.viewmodel.team.AddTeamViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddTeamScreen(
    navigation: INavigationRouter,
    viewModel: AddTeamViewModel = hiltViewModel(),
) {
    val uiState = viewModel.uiState
    val isDark = isSystemInDarkTheme()
    val placeholderColor = if (isDark) DarkPlaceholder else LightPlaceholder

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            viewModel.onSuccessConsumed()
            navigation.navigateToClubManagementScreen()
        }
    }

    BaseScreen(
        title = stringResource(if (uiState.isEditMode) R.string.edit_team_title else R.string.add_team_title),
        onBack = { navigation.returnBack() },
        trailingIcon = if (uiState.isEditMode) {
            {
                IconButton(onClick = { viewModel.onDeleteTeam() }) {
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

            // ── Team photo upload ─────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.onLogoUploadTap() },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.camera),
                        contentDescription = stringResource(R.string.add_team_photo_cd),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.size(32.dp),
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = stringResource(R.string.add_team_brand_label),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
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
                        tint = MaterialTheme.colorScheme.primary,
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
            ExposedDropdownMenuBox(
                expanded = uiState.headCoachSuggestions.isNotEmpty(),
                onExpandedChange = {},
                modifier = Modifier.fillMaxWidth(),
            ) {
                OutlinedTextField(
                    value = uiState.headCoachQuery,
                    onValueChange = { viewModel.onHeadCoachQueryChange(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    placeholder = {
                        Text(
                            text = stringResource(R.string.add_team_head_coach_placeholder),
                            style = MaterialTheme.typography.bodyMedium,
                            color = placeholderColor,
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
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

                ExposedDropdownMenu(
                    expanded = uiState.headCoachSuggestions.isNotEmpty(),
                    onDismissRequest = {},
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface),
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
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
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

            ExposedDropdownMenuBox(
                expanded = uiState.assistantSuggestions.isNotEmpty(),
                onExpandedChange = {},
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedTextField(
                        value = uiState.assistantQuery,
                        onValueChange = { viewModel.onAssistantQueryChange(it) },
                        modifier = Modifier
                            .weight(1f)
                            .menuAnchor(),
                        placeholder = {
                            Text(
                                text = stringResource(R.string.add_team_assistant_placeholder),
                                style = MaterialTheme.typography.bodyMedium,
                                color = placeholderColor,
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
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

                    // Orange + button
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primary)
                            .clickable { viewModel.onAssistantAdd() },
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(R.string.add_team_add_assistant_cd),
                            tint = Color.White,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }

                ExposedDropdownMenu(
                    expanded = uiState.assistantSuggestions.isNotEmpty(),
                    onDismissRequest = {},
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface),
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
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                    modifier = Modifier.size(16.dp),
                                )
                            },
                        )
                    }
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
                            tint = MaterialTheme.colorScheme.primary,
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
                                Icons.Default.KeyboardArrowUp
                            else
                                Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
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
                    text = uiState.error,
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
private fun AssistantChip(
    name: String,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
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
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.size(12.dp),
            )
        }
    }
}
