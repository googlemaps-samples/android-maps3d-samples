package com.example.advancedmaps3dsamples.ainavigator
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

/**
 * A composable that displays a customizable aviation-style "whiskey" compass.
 *
 * This version allows detailed control over visual elements including text styles,
 * tick sizes, and stroke widths.
 *
 * @param heading The current heading in degrees (0-360).
 * @param modifier The modifier to be applied to the compass.
 * @param backgroundColor The background color of the compass strip.
 * @param tickColor The color of the tick marks on the rotating dial.
 * @param labelColor The default color for the cardinal and numeric heading text,
 * used if the provided TextStyles don't specify a color.
 * @param lubberLineColor The color of the central indicator line.
 * @param pixelsPerDegree Controls the horizontal spacing between degree markers on the dial.
 * @param cardinalTextStyle TextStyle for the cardinal direction (e.g., "N", "NE").
 * @param numericTextStyle TextStyle for the numeric heading (e.g., "45").
 * @param majorTickHeight Height of the major tick marks.
 * @param minorTickHeight Height of the minor tick marks.
 * @param tickAreaHeight The height of the central area where the lubber line and ticks are displayed.
 * @param majorTickStrokeWidth Stroke width for major ticks.
 * @param minorTickStrokeWidth Stroke width for minor ticks.
 * @param lubberLineStrokeWidth Stroke width for the lubber line.
 */
@Composable
fun WhiskeyCompass(
    heading: Float,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.primary,
    tickColor: Color = MaterialTheme.colorScheme.onPrimary,
    labelColor: Color = MaterialTheme.colorScheme.onPrimary,
    lubberLineColor: Color = Color.Red,
    pixelsPerDegree: Float = 15f,
    cardinalTextStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    numericTextStyle: TextStyle = MaterialTheme.typography.headlineSmall,
    majorTickHeight: Dp = 25.dp,
    minorTickHeight: Dp = 15.dp,
    tickAreaHeight: Dp = 40.dp, // Defines the central band for ticks and lubber line
    majorTickStrokeWidth: Dp = 2.dp,
    minorTickStrokeWidth: Dp = 1.dp,
    lubberLineStrokeWidth: Dp = 3.dp
) {

    Box(
        modifier = modifier.background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        // The rotating compass card with only tick marks
        Canvas(modifier = Modifier.fillMaxWidth()) {
            val canvasWidth = size.width
            val canvasCenterY = center.y // Center Y of the entire canvas
            val xOffset = size.center.x - (heading * pixelsPerDegree)

            // Calculate Y positions for ticks to be somewhat centered if canvas is taller than tickAreaHeight
            // Ticks will be drawn from the top edge of the tick drawing area.
            val tickDrawingAreaStartY = canvasCenterY - (tickAreaHeight.toPx() / 2f)

            translate(left = xOffset) {
                // Draw from -360 to 0, then 0 to 360, and 360 to 720 for robust wrapping.
                for (repetition in -1..1) {
                    val repetitionOffset = repetition * 360 * pixelsPerDegree
                    for (degree in 0 until 360) {
                        val xPos = degree * pixelsPerDegree + repetitionOffset

                        // Only draw elements that might be visible
                        if (xPos > -xOffset - (pixelsPerDegree * 10) && xPos < -xOffset + canvasWidth + (pixelsPerDegree * 10)) {
                            // Major ticks every 10 degrees
                            if (degree % 10 == 0) {
                                drawLine(
                                    color = tickColor,
                                    start = Offset(x = xPos, y = tickDrawingAreaStartY),
                                    end = Offset(x = xPos, y = tickDrawingAreaStartY + majorTickHeight.toPx()),
                                    strokeWidth = majorTickStrokeWidth.toPx()
                                )
                            }
                            // Minor ticks every 5 degrees
                            else if (degree % 5 == 0) {
                                drawLine(
                                    color = tickColor,
                                    start = Offset(x = xPos, y = tickDrawingAreaStartY),
                                    end = Offset(x = xPos, y = tickDrawingAreaStartY + minorTickHeight.toPx()),
                                    strokeWidth = minorTickStrokeWidth.toPx()
                                )
                            }
                        }
                    }
                }
            }
        }

        // Static elements that do not move
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Cardinal direction text (e.g., NE)
            Text(
                text = heading.toCardinalDirection(),
                style = cardinalTextStyle.copy(
                    color = if (cardinalTextStyle.color == Color.Unspecified) labelColor else cardinalTextStyle.color
                )
            )

            // The fixed central lubber line area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(tickAreaHeight)
            ) {
                Canvas(modifier = Modifier.matchParentSize()) { // Use matchParentSize to fill the Box
                    drawLine(
                        color = lubberLineColor,
                        start = Offset(x = center.x, y = 0f),
                        end = Offset(x = center.x, y = size.height), // spans full height of this Box
                        strokeWidth = lubberLineStrokeWidth.toPx()
                    )
                }
            }

            // Numeric heading text (e.g., 45)
            Text(
                text = heading.roundToInt().toString(),
                style = numericTextStyle.copy(
                    color = if (numericTextStyle.color == Color.Unspecified) labelColor else numericTextStyle.color
                )
            )
        }
    }
}


/**
 * Converts a heading in degrees to its corresponding cardinal or intercardinal direction.
 */
private fun Float.toCardinalDirection(): String {
    // Ensure heading is within 0-360 range for calculations
    val normalizedHeading = (this % 360 + 360) % 360
    val directions = listOf("N", "NE", "E", "SE", "S", "SW", "W", "NW", "N")
    // Add 22.5 for correct centering of 45 degree segments, then divide by 45
    return directions[((normalizedHeading + 22.5f) / 45f).toInt() % 8]
}

@Preview(showBackground = true, backgroundColor = 0xFF222222)
@Composable
private fun FullyCustomizableWhiskeyCompassPreview() {
    val exampleHeadings = listOf(
        0f, 45f, 168f, 225f, 358f
    )

    Column(
        modifier = Modifier
            .background(Color.DarkGray)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text("Default Styling", color = Color.White, style = MaterialTheme.typography.titleMedium)
        WhiskeyCompass(
            heading = 45f,
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp) // Give it some space
        )

        Spacer(modifier = Modifier.height(16.dp))
        Text("Custom Scaled & Styled", color = Color.White, style = MaterialTheme.typography.titleMedium)
        WhiskeyCompass(
            heading = 123f,
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp), // Taller
            backgroundColor = Color(0xFF303030),
            tickColor = Color.Cyan,
            labelColor = Color.Yellow,
            lubberLineColor = Color.Magenta,
            pixelsPerDegree = 20f, // More spread out
            cardinalTextStyle = MaterialTheme.typography.headlineMedium.copy(
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold
            ),
            numericTextStyle = MaterialTheme.typography.displaySmall.copy(
                fontFamily = FontFamily.Monospace,
                letterSpacing = 2.sp
            ),
            majorTickHeight = 35.dp, // Taller ticks
            minorTickHeight = 20.dp,
            tickAreaHeight = 60.dp, // Larger central band
            majorTickStrokeWidth = 3.dp,
            minorTickStrokeWidth = 1.5.dp,
            lubberLineStrokeWidth = 4.dp
        )

        Spacer(modifier = Modifier.height(16.dp))
        Text("Minimalist Small Version", color = Color.White, style = MaterialTheme.typography.titleMedium)
        WhiskeyCompass(
            heading = 270f,
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp), // Compact
            pixelsPerDegree = 10f, // Denser
            cardinalTextStyle = MaterialTheme.typography.labelSmall,
            numericTextStyle = MaterialTheme.typography.titleSmall,
            majorTickHeight = 15.dp,
            minorTickHeight = 8.dp,
            tickAreaHeight = 25.dp,
            majorTickStrokeWidth = 1.5.dp,
            minorTickStrokeWidth = 0.5.dp,
            lubberLineStrokeWidth = 2.dp
        )


        exampleHeadings.forEach { heading ->
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Test Heading: ${heading.toInt()}° (${heading.toCardinalDirection()})",
                color = Color.LightGray,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp
            )
            WhiskeyCompass(
                heading = heading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                cardinalTextStyle = MaterialTheme.typography.bodySmall, // Smaller for these examples
                numericTextStyle = MaterialTheme.typography.bodyLarge,
                tickAreaHeight = 30.dp,
                majorTickHeight = 20.dp,
                minorTickHeight = 10.dp
            )
        }
    }
}
