package com.kourt.app.ui.components.bottomSheets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kourt.app.R
import com.kourt.app.ui.components.authTextFieldColors
import com.kourt.app.ui.theme.KourtTheme
import com.kourt.app.ui.theme.White

private val ButtonShape = RoundedCornerShape(50.dp)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamIdentityBottomSheet(
    selectedColor: Color,
    selectedInitials: String,
    onDismiss: () -> Unit,
    onApply: (color: Color, initials: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var pendingColor by remember(selectedColor) { mutableStateOf(selectedColor) }
    var pendingInitials by remember(selectedInitials) { mutableStateOf(selectedInitials) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        TeamIdentitySheetContent(
            pendingColor = pendingColor,
            pendingInitials = pendingInitials,
            onColorSelect = { pendingColor = it },
            onInitialsChange = { pendingInitials = it.uppercase().take(3) },
            onDismiss = onDismiss,
            onApply = { onApply(pendingColor, pendingInitials); onDismiss() },
        )
    }
}

@Composable
private fun TeamIdentitySheetContent(
    pendingColor: Color,
    pendingInitials: String,
    onColorSelect: (Color) -> Unit,
    onInitialsChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onApply: () -> Unit,
) {
    val placeholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp)
            .padding(bottom = 16.dp),
    ) {
        Text(
            text = stringResource(R.string.team_identity_title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.team_identity_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        )

        Spacer(Modifier.height(24.dp))

        // ── Live preview row ─────────────────────────────────────────────────
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            // Mini avatar preview
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(pendingColor),
            ) {
                Text(
                    text = pendingInitials.take(3),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = pendingColor.contentColorForBackground(),
                )
            }

            // Abbreviation field
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.team_identity_abbr_label),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                )
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = pendingInitials,
                    onValueChange = onInitialsChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = stringResource(R.string.team_identity_abbr_placeholder),
                            style = MaterialTheme.typography.bodyMedium,
                            color = placeholderColor,
                        )
                    },
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        textAlign = TextAlign.Center,
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        capitalization = KeyboardCapitalization.Characters,
                        imeAction = ImeAction.Done,
                    ),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = authTextFieldColors(placeholderColor = placeholderColor),
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        // ── Color grid ───────────────────────────────────────────────────────
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            items(AccentColors) { color ->
                IdentityColorCircle(
                    color = color,
                    selected = color == pendingColor,
                    onClick = { onColorSelect(color) },
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        // ── Buttons ──────────────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = ButtonShape,
            ) {
                Text(
                    text = stringResource(R.string.color_picker_cancel),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            Button(
                onClick = onApply,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = ButtonShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = White,
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 0.dp,
                ),
            ) {
                Text(
                    text = stringResource(R.string.team_identity_apply),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

@Composable
private fun IdentityColorCircle(
    color: Color,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(color)
            .then(
                if (selected) Modifier.border(3.dp, White, CircleShape)
                else Modifier
            )
            .clickable(onClick = onClick),
    ) {
        if (selected) {
            Icon(
                painter = painterResource(R.drawable.check),
                contentDescription = null,
                tint = White,
                modifier = Modifier.size(28.dp),
            )
        }
    }
}

// ── Previews ─────────────────────────────────────────────────────────────────

@Preview(name = "TeamIdentityBottomSheet — Dark", showBackground = true, backgroundColor = 0xFF1A1918)
@Composable
private fun TeamIdentityBottomSheetDarkPreview() {
    KourtTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.surface) {
            TeamIdentitySheetContent(
                pendingColor = AccentColors[0],
                pendingInitials = "M",
                onColorSelect = {},
                onInitialsChange = {},
                onDismiss = {},
                onApply = {},
            )
        }
    }
}

@Preview(name = "TeamIdentityBottomSheet — Light", showBackground = true, backgroundColor = 0xFFEEF0F8)
@Composable
private fun TeamIdentityBottomSheetLightPreview() {
    KourtTheme(darkTheme = false) {
        Surface(color = MaterialTheme.colorScheme.surface) {
            TeamIdentitySheetContent(
                pendingColor = AccentColors[2],
                pendingInitials = "W",
                onColorSelect = {},
                onInitialsChange = {},
                onDismiss = {},
                onApply = {},
            )
        }
    }
}
