package com.example.advancedmaps3dsamples.ainavigator // Keep your package structure

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column // Keep for Preview if needed, not for main compass
import androidx.compose.foundation.layout.Spacer // Keep for Preview
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding // Keep for Preview
import androidx.compose.foundation.rememberScrollState // Keep for Preview
import androidx.compose.foundation.verticalScroll // Keep for Preview
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text // Keep for Preview
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily // Keep for Preview
import androidx.compose.ui.text.font.FontWeight // Keep for Preview
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp // Keep for Preview
import kotlin.math.roundToInt

/**
 * A composable that displays a customizable aviation-style "whiskey" compass.
 * This version renders a flat, scrollable strip with ticks, degree labels, and cardinal directions.
 *
 * @param heading The current heading in degrees (0-360).
 * @param modifier The modifier to be applied to the compass.
 * @param stripHeight The total visual height of the compass strip.
 * @param backgroundColor The background color of the compass strip.
 * @param tickColor The color of the tick marks on the rotating dial.
 * @param lubberLineColor The color of the central indicator line.
 * @param pixelsPerDegree Controls the horizontal spacing between degree markers on the dial.
 *
 * @param showDegreeLabels Whether to display numeric degree labels on the strip.
 * @param degreeLabelInterval Interval for numeric degree labels (e.g., every 15 degrees).
 * @param degreeLabelTextStyle TextStyle for the numeric degree labels on the strip.
 * @param degreeLabelVerticalOffset Vertical offset of degree labels from the bottom of the ticks.
 *
 * @param showCardinalLabels Whether to display cardinal direction labels (N, NE, E, etc.) on the strip.
 * @param cardinalLabelTextStyle TextStyle for the cardinal direction labels on the strip.
 * @param cardinalLabelVerticalOffset Vertical offset of cardinal labels from the top of the ticks.
 *
 * @param majorTickHeight Height of the major tick marks.
 * @param minorTickHeight Height of the minor tick marks.
 * @param majorTickStrokeWidth Stroke width for major ticks.
 * @param minorTickStrokeWidth Stroke width for minor ticks.
 * @param lubberLineStrokeWidth Stroke width for the lubber line.
 */
@Composable
fun WhiskeyCompass(
    heading: Float,
    modifier: Modifier = Modifier,
    stripHeight: Dp = 80.dp,
    backgroundColor: Color = MaterialTheme.colorScheme.primaryContainer,
    tickColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    lubberLineColor: Color = Color.Red,
    pixelsPerDegree: Float = 10f,

    showDegreeLabels: Boolean = true,
    degreeLabelInterval: Int = 15,
    degreeLabelTextStyle: TextStyle = MaterialTheme.typography.labelSmall.copy(textAlign = TextAlign.Center),
    degreeLabelVerticalOffset: Dp = 4.dp,

    showCardinalLabels: Boolean = true,
    cardinalLabelTextStyle: TextStyle = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, textAlign = TextAlign.Center),
    cardinalLabelVerticalOffset: Dp = 4.dp,

    majorTickHeight: Dp = 25.dp,
    minorTickHeight: Dp = 15.dp,
    majorTickStrokeWidth: Dp = 2.dp,
    minorTickStrokeWidth: Dp = 1.dp,
    lubberLineStrokeWidth: Dp = 2.dp
) {
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current

    // Memoize measured cardinal labels for performance
    val measuredCardinalLabels = remember(cardinalLabelTextStyle, density) {
        // Pass density to measure if text style uses Dp for size, though typically it's Sp.
        // For safety or if a custom TextStyle using Dp might be passed.
        with(density) {
            mapOf(
                0 to textMeasurer.measure("N", style = cardinalLabelTextStyle),
                45 to textMeasurer.measure("NE", style = cardinalLabelTextStyle),
                90 to textMeasurer.measure("E", style = cardinalLabelTextStyle),
                135 to textMeasurer.measure("SE", style = cardinalLabelTextStyle),
                180 to textMeasurer.measure("S", style = cardinalLabelTextStyle),
                225 to textMeasurer.measure("SW", style = cardinalLabelTextStyle),
                270 to textMeasurer.measure("W", style = cardinalLabelTextStyle),
                315 to textMeasurer.measure("NW", style = cardinalLabelTextStyle)
            )
        }
    }


    Box(
        modifier = modifier
            .height(stripHeight)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        // Canvas 1: Scrolling Compass Strip (ticks, degree labels, cardinal labels)
        Canvas(modifier = Modifier.matchParentSize()) {
            // Convert Dp to Px here, inside DrawScope where density is implicitly available
            val majorTickHeightPx = majorTickHeight.toPx()
            val minorTickHeightPx = minorTickHeight.toPx()
            val majorTickStrokeWidthPx = majorTickStrokeWidth.toPx()
            val minorTickStrokeWidthPx = minorTickStrokeWidth.toPx()
            val degreeLabelVerticalOffsetPx = degreeLabelVerticalOffset.toPx()
            val cardinalLabelVerticalOffsetPx = cardinalLabelVerticalOffset.toPx()

            val canvasWidth = size.width
            val canvasCenterY = center.y // Vertical center of the strip

            // xOffset determines how much the strip is shifted horizontally
            val xOffset = center.x - (heading * pixelsPerDegree)

            // The Y position where the center line of the ticks should be.
            // Labels will be drawn above/below this.
            val tickCenterY = canvasCenterY

            translate(left = xOffset) {
                // Loop through repetitions to ensure seamless wrapping
                for (repetition in -1..1) {
                    val repetitionBaseDegree = repetition * 360
                    for (degreeInRepetition in 0 until 360) {
                        val absoluteDegree = repetitionBaseDegree + degreeInRepetition
                        val xPos = absoluteDegree * pixelsPerDegree

                        // Optimization: Only draw if potentially visible
                        val visibilityMargin = canvasWidth // Generous margin
                        if (xPos < -xOffset + canvasWidth + visibilityMargin && xPos > -xOffset - visibilityMargin) {

                            val isMajorTickEquivalent = degreeInRepetition % 10 == 0
                            val isMinorTickEquivalent = degreeInRepetition % 5 == 0 && !isMajorTickEquivalent

                            // Draw Major Ticks (every 10 degrees)
                            if (isMajorTickEquivalent) {
                                val tickTopY = tickCenterY - majorTickHeightPx / 2f
                                val tickBottomY = tickCenterY + majorTickHeightPx / 2f
                                drawLine(
                                    color = tickColor,
                                    start = Offset(x = xPos, y = tickTopY),
                                    end = Offset(x = xPos, y = tickBottomY),
                                    strokeWidth = majorTickStrokeWidthPx
                                )

                                // Draw Cardinal Labels above major ticks
                                if (showCardinalLabels && measuredCardinalLabels.containsKey(degreeInRepetition)) {
                                    val measuredText = measuredCardinalLabels.getValue(degreeInRepetition)
                                    drawText(
                                        textLayoutResult = measuredText,
                                        topLeft = Offset(
                                            x = xPos - measuredText.size.width / 2f,
                                            y = tickTopY - measuredText.size.height - cardinalLabelVerticalOffsetPx
                                        )
                                    )
                                }
                            }
                            // Draw Minor Ticks (every 5 degrees, not overlapping major)
                            else if (isMinorTickEquivalent) {
                                val tickTopY = tickCenterY - minorTickHeightPx / 2f
                                val tickBottomY = tickCenterY + minorTickHeightPx / 2f
                                drawLine(
                                    color = tickColor,
                                    start = Offset(x = xPos, y = tickTopY),
                                    end = Offset(x = xPos, y = tickBottomY),
                                    strokeWidth = minorTickStrokeWidthPx
                                )
                            }

                            // Draw Degree Labels below ticks at specified intervals
                            if (showDegreeLabels && degreeInRepetition % degreeLabelInterval == 0) {
                                val tickBottomY = tickCenterY + (if (isMajorTickEquivalent) majorTickHeightPx else if (isMinorTickEquivalent) minorTickHeightPx else 0f) / 2f
                                val labelText = degreeInRepetition.toString()
                                // It's good practice to use density for text measurement if TextStyle might involve Dp.
                                // For standard Sp, it's usually handled, but explicit density in remember is safer.
                                val measuredText = textMeasurer.measure(labelText, style = degreeLabelTextStyle)
                                drawText(
                                    textLayoutResult = measuredText,
                                    topLeft = Offset(
                                        x = xPos - measuredText.size.width / 2f,
                                        y = tickBottomY + degreeLabelVerticalOffsetPx
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // Canvas 2: Fixed Lubber Line
        Canvas(modifier = Modifier.matchParentSize()) {
            // Draw the lubber line vertically centered, spanning a good portion of the strip height
            val lubberLineVisualPadding = stripHeight.toPx() * 0.05f // Uses DrawScope.toPx() implicitly
            val currentLubberLineStrokeWidthPx = lubberLineStrokeWidth.toPx() // Uses DrawScope.toPx() implicitly
            drawLine(
                color = lubberLineColor,
                start = Offset(x = center.x, y = lubberLineVisualPadding),
                end = Offset(x = center.x, y = size.height - lubberLineVisualPadding),
                strokeWidth = currentLubberLineStrokeWidthPx
            )
        }
    }
}

/**
 * Converts a heading in degrees to its corresponding cardinal or intercardinal direction.
 * (This is not used by the compass itself anymore but kept for previews/external use)
 */
private fun Float.toCardinalDirection(): String {
    val normalizedHeading = (this % 360 + 360) % 360
    val directions = listOf("N", "NE", "E", "SE", "S", "SW", "W", "NW", "N")
    return directions[((normalizedHeading + 22.5f) / 45f).toInt() % 8]
}

@Preview(showBackground = true, backgroundColor = 0xFF222222)
@Composable
private fun FlatWhiskeyCompassPreview() {
    val exampleHeadings = listOf(
        0f, 10f, 22.5f, 45f, 168f, 270f, 358f
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.DarkGray)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Default Flat Compass Strip", color = Color.White, style = MaterialTheme.typography.titleMedium)
        WhiskeyCompass(
            heading = 45f,
            modifier = Modifier.fillMaxWidth(),
            stripHeight = 100.dp,
            pixelsPerDegree = 8f // Make it a bit denser for preview
        )

        Spacer(modifier = Modifier.height(16.dp))
        Text("Customized Labels & Ticks", color = Color.White, style = MaterialTheme.typography.titleMedium)
        WhiskeyCompass(
            heading = 123f,
            modifier = Modifier.fillMaxWidth(),
            stripHeight = 120.dp,
            backgroundColor = Color(0xFF1A237E), // Dark Blue
            tickColor = Color(0xFFB0BEC5), // Blue Grey
            lubberLineColor = Color(0xFFFFD600), // Amber
            pixelsPerDegree = 12f,
            degreeLabelInterval = 10,
            degreeLabelTextStyle = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF81D4FA)), // Light Blue
            cardinalLabelTextStyle = MaterialTheme.typography.labelLarge.copy(color = Color.White, fontWeight = FontWeight.Bold),
            majorTickHeight = 30.dp,
            minorTickHeight = 18.dp,
            degreeLabelVerticalOffset = 6.dp,
            cardinalLabelVerticalOffset = 6.dp,
            majorTickStrokeWidth = 2.5.dp
        )

        Spacer(modifier = Modifier.height(16.dp))
        Text("No Cardinal Labels", color = Color.White, style = MaterialTheme.typography.titleMedium)
        WhiskeyCompass(
            heading = 210f,
            modifier = Modifier.fillMaxWidth(),
            stripHeight = 70.dp,
            showCardinalLabels = false,
            pixelsPerDegree = 6f
        )

        Spacer(modifier = Modifier.height(16.dp))
        Text("No Degree Labels", color = Color.White, style = MaterialTheme.typography.titleMedium)
        WhiskeyCompass(
            heading = 300f,
            modifier = Modifier.fillMaxWidth(),
            stripHeight = 70.dp,
            showDegreeLabels = false,
            pixelsPerDegree = 6f
        )


        exampleHeadings.forEach { currentHeading ->
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Test Heading: ${currentHeading.roundToInt()}°",
                color = Color.LightGray,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp
            )
            WhiskeyCompass(
                heading = currentHeading,
                modifier = Modifier.fillMaxWidth(),
                stripHeight = 90.dp,
                pixelsPerDegree = 7f,
                degreeLabelInterval = 30 // Less frequent degree labels for clarity
            )
        }
    }
}
