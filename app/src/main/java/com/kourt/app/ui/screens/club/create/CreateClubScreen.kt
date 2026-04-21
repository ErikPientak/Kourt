package com.kourt.app.ui.screens.club.create

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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
import com.kourt.app.ui.components.KourtButton
import com.kourt.app.ui.components.authTextFieldColors
import com.kourt.app.ui.theme.DarkPlaceholder
import com.kourt.app.ui.theme.LightPlaceholder
import com.kourt.app.viewmodel.club.CreateClubViewModel

private val COUNTRIES = listOf(
    "USA",
    "Canada",
    "Czech Republic",
    "Slovakia",
    "Germany",
    "France",
    "UK",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateClubScreen(
    navigation: INavigationRouter,
    viewModel: CreateClubViewModel = hiltViewModel(),
) {
    val uiState = viewModel.uiState
    val placeholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
    val iconTintColor = MaterialTheme.colorScheme.onBackground

    var countryDropdownExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            navigation.navigateToReviewConfirm(
                clubName = uiState.clubName,
                shortName = uiState.shortName,
                president = uiState.president,
                technicalDirector = uiState.technicalDirector,
                country = uiState.country,
                city = uiState.city,
            )
            viewModel.onSaveConsumed()
        }
    }

    BaseScreen(
        title = stringResource(R.string.create_club_title),
        onBack = { navigation.returnBack() },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // ── Logo upload area ─────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.onLogoUploadTap() },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.camera),
                        contentDescription = stringResource(R.string.create_club_upload_logo),
                        tint = iconTintColor,
                        modifier = Modifier.size(36.dp),
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = stringResource(R.string.create_club_upload_logo),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = stringResource(R.string.create_club_upload_logo_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ── Club Name ────────────────────────────────────────────────────
            AuthFieldLabel(label = stringResource(R.string.club_name_label))
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = uiState.clubName,
                onValueChange = { viewModel.onClubNameChange(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = stringResource(R.string.club_name_placeholder),
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

            // ── Short Name / Abbreviation ────────────────────────────────────
            AuthFieldLabel(label = stringResource(R.string.short_name_label))
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = uiState.shortName,
                onValueChange = { viewModel.onShortNameChange(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = stringResource(R.string.short_name_placeholder),
                        style = MaterialTheme.typography.bodyMedium,
                        color = placeholderColor,
                    )
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.short_text),
                        contentDescription = null,
                        tint = iconTintColor,
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    capitalization = KeyboardCapitalization.Characters,
                    imeAction = ImeAction.Next,
                ),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = authTextFieldColors(placeholderColor = placeholderColor),
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ── Club President ───────────────────────────────────────────────
            AuthFieldLabel(label = stringResource(R.string.club_president_label))
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = uiState.president,
                onValueChange = { viewModel.onPresidentChange(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = stringResource(R.string.club_president_placeholder),
                        style = MaterialTheme.typography.bodyMedium,
                        color = placeholderColor,
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Person,
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

            // ── Technical Director ───────────────────────────────────────────
            AuthFieldLabel(label = stringResource(R.string.technical_director_label))
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = uiState.technicalDirector,
                onValueChange = { viewModel.onTechnicalDirectorChange(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = stringResource(R.string.technical_director_placeholder),
                        style = MaterialTheme.typography.bodyMedium,
                        color = placeholderColor,
                    )
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.admin),
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

            // ── Country + Primary City row ───────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top,
            ) {
                // Country dropdown
                Column(modifier = Modifier.weight(0.45f)) {
                    AuthFieldLabel(label = stringResource(R.string.country_label))
                    Spacer(modifier = Modifier.height(6.dp))
                    ExposedDropdownMenuBox(
                        expanded = countryDropdownExpanded,
                        onExpandedChange = { countryDropdownExpanded = it },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        OutlinedTextField(
                            value = uiState.country,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = authTextFieldColors(placeholderColor = placeholderColor),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface,
                            ),
                        )

                        ExposedDropdownMenu(
                            expanded = countryDropdownExpanded,
                            onDismissRequest = { countryDropdownExpanded = false },
                            modifier = Modifier.background(MaterialTheme.colorScheme.surface),
                        ) {
                            COUNTRIES.forEach { country ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = country,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                        )
                                    },
                                    onClick = {
                                        viewModel.onCountryChange(country)
                                        countryDropdownExpanded = false
                                    },
                                )
                            }
                        }
                    }
                }

                // Primary City
                Column(modifier = Modifier.weight(0.55f)) {
                    AuthFieldLabel(label = stringResource(R.string.city_label))
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = uiState.city,
                        onValueChange = { viewModel.onCityChange(it) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                text = stringResource(R.string.city_placeholder),
                                style = MaterialTheme.typography.bodyMedium,
                                color = placeholderColor,
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
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── Error message ────────────────────────────────────────────────
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

            // ── Review & Confirm button ──────────────────────────────────────
            KourtButton(
                text = stringResource(R.string.create_club_review_button),
                onClick = { viewModel.onCreateClub() },
                enabled = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ── Terms of Service footer ──────────────────────────────────────
            Row(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = stringResource(R.string.create_club_terms),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
                Text(
                    text = stringResource(R.string.create_club_terms_link),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { },
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
