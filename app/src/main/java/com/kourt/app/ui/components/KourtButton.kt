package com.kourt.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kourt.app.ui.theme.White
import kourtNeonGlow

private val KourtButtonShape = RoundedCornerShape(50.dp)

/**
 * Primary CTA button used throughout the Kourt app.
 *
 * Pill-shaped orange button. In dark mode, three concentric rounded-rect layers
 * behind the button produce a visible orange bloom effect.
 */
//@Composable
//fun KourtButton(
//    text: String,
//    onClick: () -> Unit,
//    modifier: Modifier = Modifier,
//    enabled: Boolean = true,
//) {
//    val primaryColor = MaterialTheme.colorScheme.primary
//
//    Box(
//        contentAlignment = Alignment.Center,
//        modifier = modifier.height(80.dp),  // 52dp button + 14dp glow room top & bottom
//    ) {
//        Button(
//            onClick = onClick,
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(52.dp),
//            enabled = enabled,
//            shape = KourtButtonShape,
//            colors = ButtonDefaults.buttonColors(
//                containerColor = primaryColor,
//                contentColor = White,
//                disabledContainerColor = primaryColor.copy(alpha = 0.38f),
//                disabledContentColor = White.copy(alpha = 0.6f),
//            ),
//            elevation = ButtonDefaults.buttonElevation(
//                defaultElevation = 0.dp,
//                pressedElevation = 0.dp,
//                focusedElevation = 0.dp,
//                hoveredElevation = 0.dp,
//                disabledElevation = 0.dp,
//            ),
//        ) {
//            Text(
//                text = text,
//                style = MaterialTheme.typography.labelLarge,
//            )
//        }
//    }
//}

@Composable
fun KourtButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val primaryColor = MaterialTheme.colorScheme.primary

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.height(80.dp),
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                // We apply the glow here.
                // 14.dp matches your "glow room" height logic
                .kourtNeonGlow(
                    color = primaryColor,
                    shapeRadius = 26.dp,
                    enabled = enabled
                ),
            enabled = enabled,
            shape = KourtButtonShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = primaryColor,
                contentColor = White,
                disabledContainerColor = primaryColor.copy(alpha = 0.38f),
                disabledContentColor = White.copy(alpha = 0.6f),
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000) // Dark background makes it pop!
@Composable
fun KourtButtonNeonPreview() {
    MaterialTheme(colorScheme = darkColorScheme(primary = Color(0xFF00E5FF))) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            KourtButton(
                text = "NEON ACTIVE",
                onClick = {}
            )

            KourtButton(
                text = "NEON DISABLED",
                enabled = false,
                onClick = {}
            )
        }
    }
}