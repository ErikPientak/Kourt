package com.kourt.app.ui.screens.auth.register

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kourt.app.R
import com.kourt.app.navigation.INavigationRouter
import com.kourt.app.ui.components.AuthFieldLabel
import com.kourt.app.ui.components.KourtButton
import com.kourt.app.ui.components.authTextFieldColors
import com.kourt.app.viewmodel.auth.RegisterViewModel

@Composable
fun RegisterScreen(
    navigation: INavigationRouter,
    viewModel: RegisterViewModel = hiltViewModel(),
) {
    val uiState = viewModel.uiState

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            viewModel.onSaveConsumed()
            navigation.navigateToHome()
        }
    }

    RegisterScreenContent(
        uiState = uiState,
        actions = viewModel,
        onNavigateToLogin = { navigation.navigateToLoginScreen() },
    )
}

@Composable
private fun RegisterScreenContent(
    uiState: RegisterScreenUiState,
    actions: RegisterScreenActions,
    onNavigateToLogin: () -> Unit,
) {
    var fullName by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var confirmPasswordVisible by rememberSaveable { mutableStateOf(false) }

    val placeholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
    val iconTintColor = MaterialTheme.colorScheme.onBackground

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center,
    ) {
        Spacer(modifier = Modifier.height(64.dp))

        Text(
            text = stringResource(R.string.auth_join),
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.primary,
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(R.string.auth_register_subtitle),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(36.dp))

        // Full Name field
        AuthFieldLabel(label = stringResource(R.string.auth_full_name_label))
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = stringResource(R.string.auth_full_name_placeholder),
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
            shape = RoundedCornerShape(52.dp),
            colors = authTextFieldColors(placeholderColor = placeholderColor),
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Email field
        AuthFieldLabel(label = stringResource(R.string.auth_email_label))
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = stringResource(R.string.auth_email_placeholder),
                    style = MaterialTheme.typography.bodyMedium,
                    color = placeholderColor,
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Email,
                    contentDescription = null,
                    tint = iconTintColor,
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
            ),
            singleLine = true,
            shape = RoundedCornerShape(52.dp),
            colors = authTextFieldColors(placeholderColor = placeholderColor),
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Password field
        AuthFieldLabel(label = stringResource(R.string.auth_password_label))
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = stringResource(R.string.auth_password_placeholder),
                    style = MaterialTheme.typography.bodyMedium,
                    color = placeholderColor,
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = null,
                    tint = iconTintColor,
                )
            },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        painter = painterResource(
                            id = if (passwordVisible) R.drawable.visibility_off else R.drawable.visibility
                        ),
                        contentDescription = stringResource(
                            if (passwordVisible) R.string.auth_password_hide else R.string.auth_password_show
                        ),
                        tint = iconTintColor,
                    )
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next,
            ),
            singleLine = true,
            shape = RoundedCornerShape(52.dp),
            colors = authTextFieldColors(placeholderColor = placeholderColor),
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Confirm Password field
        AuthFieldLabel(label = stringResource(R.string.auth_confirm_password_label))
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = stringResource(R.string.auth_confirm_password_placeholder),
                    style = MaterialTheme.typography.bodyMedium,
                    color = placeholderColor,
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = null,
                    tint = iconTintColor,
                )
            },
            trailingIcon = {
                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                    Icon(
                        painter = painterResource(
                            id = if (confirmPasswordVisible) R.drawable.visibility_off else R.drawable.visibility
                        ),
                        contentDescription = stringResource(
                            if (confirmPasswordVisible) R.string.auth_password_hide else R.string.auth_password_show
                        ),
                        tint = iconTintColor,
                    )
                }
            },
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
            ),
            singleLine = true,
            shape = RoundedCornerShape(52.dp),
            colors = authTextFieldColors(placeholderColor = placeholderColor),
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Error message
        if (uiState.error != null) {
            Text(
                text = stringResource(uiState.error),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 52.dp),
            )
        }

        // Sign Up button
        KourtButton(
            text = stringResource(R.string.auth_signup_button),
            onClick = { actions.onRegister(fullName, email, password, confirmPassword) },
            enabled = !uiState.isLoading,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(36.dp))

        // "Already have an account? Log In" footer
        Row(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = stringResource(R.string.auth_have_account),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = stringResource(R.string.auth_login_button),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { onNavigateToLogin() },
            )
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}
