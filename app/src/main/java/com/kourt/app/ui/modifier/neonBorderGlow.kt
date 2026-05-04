import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A reusable neon glow effect that draws a soft shadow layer behind the component.
 * 
 * @param color The color of the glow.
 * @param glowRadius The spread/blur distance of the glow.
 * @param shapeRadius The corner radius of the component (must match your shape).
 * @param enabled Whether the glow should be rendered.
 */
fun Modifier.kourtNeonGlow(
    color: Color,
    glowRadius: Dp = 12.dp,
    shapeRadius: Dp = 16.dp,
    enabled: Boolean = true
): Modifier = if (enabled) {
    this.drawBehind {
        val shadowColor = color.copy(alpha = 0.45f).toArgb()
        val transparentColor = color.copy(alpha = 0f).toArgb()

        drawIntoCanvas { canvas ->
            val paint = Paint()
            val frameworkPaint = paint.asFrameworkPaint()

            // Set up the native framework paint for the blur effect
            frameworkPaint.color = transparentColor
            frameworkPaint.setShadowLayer(
                glowRadius.toPx(),
                0f,
                0f,
                shadowColor
            )

            canvas.drawRoundRect(
                left = 0f,
                top = 0f,
                right = size.width,
                bottom = size.height,
                radiusX = shapeRadius.toPx(),
                radiusY = shapeRadius.toPx(),
                paint = paint
            )
        }
    }
} else this