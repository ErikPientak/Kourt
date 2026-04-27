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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kourt.app.R
import com.kourt.app.ui.theme.KourtTheme
import com.kourt.app.ui.theme.White

fun Color.contentColorForBackground(): Color {
    val luminance = 0.299f * red + 0.587f * green + 0.114f * blue
    return if (luminance > 0.5f) Color.Black else Color.White
}

val AccentColors = listOf(
    Color(0xFFFF5A25), // Orange (brand)
    Color(0xFF2563EB), // Blue
    Color(0xFF059669), // Emerald
    Color(0xFFDC2626), // Red
    Color(0xFFEAB308), // Yellow
    Color(0xFF7C3AED), // Purple
    Color(0xFF9CA3AF), // Gray
    Color(0xFF0891B2), // Cyan
    Color(0xFF1E40AF), // Navy
    Color(0xFFEC4899), // Pink
    Color(0xFF65A30D), // Lime
    Color(0xFFF97316), // Amber
)

private val ButtonShape = RoundedCornerShape(50.dp)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorPickerBottomSheet(
    selectedColor: Color,
    onDismiss: () -> Unit,
    onApply: (Color) -> Unit,
    title: String = stringResource(R.string.color_picker_title),
    subtitle: String = stringResource(R.string.color_picker_subtitle),
    modifier: Modifier = Modifier,
) {
    var pendingColor by remember(selectedColor) { mutableStateOf(selectedColor) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        ColorPickerSheetContent(
            title = title,
            subtitle = subtitle,
            pendingColor = pendingColor,
            onColorSelect = { pendingColor = it },
            onDismiss = onDismiss,
            onApply = { onApply(pendingColor); onDismiss() },
        )
    }
}

@Composable
private fun ColorPickerSheetContent(
    title: String,
    subtitle: String,
    pendingColor: Color,
    onColorSelect: (Color) -> Unit,
    onDismiss: () -> Unit,
    onApply: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp)
            .padding(bottom = 16.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        )
        Spacer(Modifier.height(24.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            items(AccentColors) { color ->
                ColorCircle(
                    color = color,
                    selected = color == pendingColor,
                    onClick = { onColorSelect(color) },
                )
            }
        }

        Spacer(Modifier.height(32.dp))

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
                    text = stringResource(R.string.color_picker_apply),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

@Composable
private fun ColorCircle(
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

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(name = "ColorPickerBottomSheet — Dark", showBackground = true, backgroundColor = 0xFF1C1917)
@Composable
private fun ColorPickerBottomSheetDarkPreview() {
    KourtTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.surface) {
            ColorPickerSheetContent(
                title = "Pick Accent Color",
                subtitle = "Select a primary color for your club",
                pendingColor = AccentColors[0],
                onColorSelect = {},
                onDismiss = {},
                onApply = {},
            )
        }
    }
}

@Preview(name = "ColorPickerBottomSheet — Light", showBackground = true, backgroundColor = 0xFFEEF0F8)
@Composable
private fun ColorPickerBottomSheetLightPreview() {
    KourtTheme(darkTheme = false) {
        Surface(color = MaterialTheme.colorScheme.surface) {
            ColorPickerSheetContent(
                title = "Pick Accent Color",
                subtitle = "Select a primary color for your club",
                pendingColor = AccentColors[2],
                onColorSelect = {},
                onDismiss = {},
                onApply = {},
            )
        }
    }
}
