package com.example.advancedmaps3dsamples.ainavigator

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

/**
 * A composable that displays a refined aviation-style "whiskey" compass.
 *
 * This version displays the precise heading numerically and as a cardinal direction,
 * fixed in the center. Behind this, a dial with tick marks rotates to align with the
 * current heading.
 *
 * @param heading The current heading in degrees (0-360).
 * @param modifier The modifier to be applied to the compass.
 * @param backgroundColor The background color of the compass strip.
 * @param foregroundColor The color of the ticks.
 * @param labelColor The color of the static heading and cardinal text.
 * @param lubberLineColor The color of the central indicator line.
 * @param pixelsPerDegree Controls the visual spacing between degree markers.
 */
@Composable
fun WhiskeyCompass(
    heading: Float,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.primary,
    foregroundColor: Color = MaterialTheme.colorScheme.onPrimary,
    labelColor: Color = MaterialTheme.colorScheme.onPrimary,
    lubberLineColor: Color = Color.Red,
    pixelsPerDegree: Float = 15f
) {

    Box(
        modifier = modifier.background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        // The rotating compass card with only tick marks
        Canvas(modifier = Modifier.fillMaxWidth()) {
            val canvasWidth = size.width
            val center = size.center

            // The horizontal offset for the entire compass card,
            // creating the rotation effect.
            val xOffset = center.x - (heading * pixelsPerDegree)

            translate(left = xOffset) {
                // Draw the compass ticks twice to handle wrap-around seamlessly
                for (repetition in -1..1) {
                    val repetitionOffset = repetition * 360 * pixelsPerDegree
                    // Draw from 0 to 360 degrees
                    for (degree in 0 until 360) {
                        val xPos = degree * pixelsPerDegree + repetitionOffset

                        // Only draw elements that might be visible to save resources
                        if (xPos > -xOffset - 50 && xPos < -xOffset + canvasWidth + 50) {
                            // Major ticks every 10 degrees
                            if (degree % 10 == 0) {
                                drawLine(
                                    color = foregroundColor,
                                    start = Offset(x = xPos, y = 0f),
                                    end = Offset(x = xPos, y = 25f),
                                    strokeWidth = 4f
                                )
                            }
                            // Minor ticks every 5 degrees
                            else if (degree % 5 == 0) {
                                drawLine(
                                    color = foregroundColor,
                                    start = Offset(x = xPos, y = 0f),
                                    end = Offset(x = xPos, y = 15f),
                                    strokeWidth = 2f
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
                color = labelColor,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )

            // The fixed central lubber line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp) // Height of the tick area
            ) {
                Canvas(modifier = Modifier.fillMaxWidth()) {
                    drawLine(
                        color = lubberLineColor,
                        start = Offset(x = center.x, y = 0f),
                        end = Offset(x = center.x, y = size.height),
                        strokeWidth = 4f
                    )
                }
            }

            // Numeric heading text (e.g., 45)
            Text(
                text = heading.roundToInt().toString(),
                color = labelColor,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Converts a heading in degrees to its corresponding cardinal or intercardinal direction.
 */
fun Float.toCardinalDirection(): String {
    val directions = listOf("N", "NE", "E", "SE", "S", "SW", "W", "NW", "N")
    return directions[((this % 360) / 45).roundToInt()]
}


@Preview(showBackground = true, backgroundColor = 0xFFCCCCCC)
@Composable
private fun UpdatedWhiskeyCompassPreview() {
    // A list of example headings to display
    val exampleHeadings = listOf(
        0f,   // North
        45f,  // North-East
        168f, // A non-cardinal direction
        180f, // South
        225f, // South-West
        358f  // Near the 360/0 wrap-around
    )

    Column(
        modifier = Modifier
            .background(Color.DarkGray)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()), // Make the column scrollable
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        exampleHeadings.forEach { heading ->
            // Label for the compass example
            Text(
                text = "Heading: ${heading.toInt()}° (${heading.toCardinalDirection()})",
                color = Color.White,
                fontFamily = FontFamily.Monospace
            )

            // The compass composable itself
            WhiskeyCompass(
                heading = heading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            )
        }
    }
}