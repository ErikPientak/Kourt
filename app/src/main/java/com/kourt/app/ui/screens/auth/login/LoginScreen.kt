package com.kourt.app.ui.screens.auth.login

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.kourt.app.R
import com.kourt.app.navigation.INavigationRouter
import com.kourt.app.ui.components.AuthFieldLabel
import com.kourt.app.ui.components.KourtButton
import com.kourt.app.ui.components.authTextFieldColors
import com.kourt.app.ui.theme.DarkPlaceholder
import com.kourt.app.ui.theme.LightPlaceholder
import com.kourt.app.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    navigation: INavigationRouter,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val uiState = viewModel.uiState
    val context = LocalContext.current

    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    val isDark = isSystemInDarkTheme()
    val placeholderColor = if (isDark) DarkPlaceholder else LightPlaceholder

    // A slightly lighter tone for the icon container background
    val iconContainerColor = if (isDark) Color(0x003A3633) else Color(0x774B2500)

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) navigation.navigateToHome()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(64.dp))

        // App icon — basketball placeholder (circle + icon)
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(iconContainerColor)
        ) {
            Icon(
                painter = painterResource(R.drawable.basketball),
                contentDescription = stringResource(R.string.app_icon_content_desc),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(44.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // "Kourt" title
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Subtitle
        Text(
            text = stringResource(R.string.app_tagline),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(40.dp))

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
                    imageVector = Icons.Default.Email,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
            ),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = authTextFieldColors(placeholderColor = placeholderColor),
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Password label row with "Forgot?" link
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.auth_password_label),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            // Forgot password — tappable text
            ClickableText(
                text = buildAnnotatedString {
                    withStyle(
                        SpanStyle(
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = MaterialTheme.typography.labelMedium.fontSize,
                            fontWeight = MaterialTheme.typography.labelMedium.fontWeight,
                            fontFamily = MaterialTheme.typography.labelMedium.fontFamily,
                        )
                    ) {
                        append(stringResource(R.string.auth_forgot_password))
                    }
                },
                onClick = { },
            )
        }

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
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
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
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
            ),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = authTextFieldColors(placeholderColor = placeholderColor),
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Error message
        if (uiState.error != null) {
            Text(
                text = uiState.error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
            )
        }

        // Log In button
        KourtButton(
            text = stringResource(R.string.auth_login_button),
            onClick = { viewModel.onSignInWithEmail(email, password) },
            enabled = !uiState.isLoading,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Sign in with Google button
        OutlinedButton(
            onClick = { viewModel.onSignInWithGoogle(context) },
            enabled = !uiState.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(50.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
            ),
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
            ),
        ) {
            Text(
                text = stringResource(R.string.auth_sign_in_google),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        Spacer(modifier = Modifier.height(36.dp))

        // "New to the court? Create an account" footer
        ClickableText(
            text = buildAnnotatedString {
                withStyle(
                    SpanStyle(
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = MaterialTheme.typography.labelMedium.fontSize,
                        fontFamily = MaterialTheme.typography.labelMedium.fontFamily,
                    )
                ) {
                    append(stringResource(R.string.auth_no_account))
                    append("  ")
                }
                withStyle(
                    SpanStyle(
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = MaterialTheme.typography.labelMedium.fontSize,
                        fontWeight = MaterialTheme.typography.labelMedium.fontWeight,
                        fontFamily = MaterialTheme.typography.labelMedium.fontFamily,
                    )
                ) {
                    append(stringResource(R.string.auth_create_account))
                }
            },
            onClick = { navigation.navigateToRegisterScreen() },
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )

        Spacer(modifier = Modifier.height(40.dp))
    }
}
