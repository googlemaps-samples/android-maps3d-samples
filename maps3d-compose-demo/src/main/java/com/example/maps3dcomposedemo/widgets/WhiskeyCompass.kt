package com.example.maps3dcomposedemo.widgets

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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    degreeLabelTextStyle: TextStyle = MaterialTheme.typography.labelSmall.copy(
        textAlign = TextAlign.Center,
    ),
    degreeLabelVerticalOffset: Dp = 4.dp,

    showCardinalLabels: Boolean = true,
    // Added for clarity
    cardinalLabelInterval: Int = 45,
    cardinalLabelTextStyle: TextStyle = MaterialTheme.typography.labelMedium.copy(
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
    ),
    cardinalLabelVerticalOffset: Dp = 4.dp,

    majorTickHeight: Dp = 25.dp,
    minorTickHeight: Dp = 15.dp,
    majorTickStrokeWidth: Dp = 2.dp,
    minorTickStrokeWidth: Dp = 1.dp,
    lubberLineStrokeWidth: Dp = 2.dp,
) {
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current

    // Normalize heading to the 0-360 range to prevent offset issues
    val normalizedHeading = (heading % 360f + 360f) % 360f

    // Memoize measured cardinal labels for performance
    val measuredCardinalLabels = remember(cardinalLabelTextStyle, density) {
        with(density) {
            mapOf(
                0 to textMeasurer.measure("N", style = cardinalLabelTextStyle),
                45 to textMeasurer.measure("NE", style = cardinalLabelTextStyle),
                90 to textMeasurer.measure("E", style = cardinalLabelTextStyle),
                135 to textMeasurer.measure("SE", style = cardinalLabelTextStyle),
                180 to textMeasurer.measure("S", style = cardinalLabelTextStyle),
                225 to textMeasurer.measure("SW", style = cardinalLabelTextStyle),
                270 to textMeasurer.measure("W", style = cardinalLabelTextStyle),
                315 to textMeasurer.measure("NW", style = cardinalLabelTextStyle),
            )
        }
    }

    Box(
        modifier = modifier
            .height(stripHeight)
            .background(backgroundColor)
            .clipToBounds(),
        contentAlignment = Alignment.Center,
    ) {
        // Canvas 1: Scrolling Compass Strip (ticks, degree labels, cardinal labels)
        Canvas(modifier = Modifier.matchParentSize()) {
            val params = CompassStripParams(
                tickColor = tickColor,
                majorTickHeightPx = majorTickHeight.toPx(),
                minorTickHeightPx = minorTickHeight.toPx(),
                majorTickStrokeWidthPx = majorTickStrokeWidth.toPx(),
                minorTickStrokeWidthPx = minorTickStrokeWidth.toPx(),
                showCardinalLabels = showCardinalLabels,
                measuredCardinalLabels = measuredCardinalLabels,
                cardinalLabelVerticalOffsetPx = cardinalLabelVerticalOffset.toPx(),
                showDegreeLabels = showDegreeLabels,
                degreeLabelInterval = degreeLabelInterval,
                degreeLabelTextStyle = degreeLabelTextStyle,
                degreeLabelVerticalOffsetPx = degreeLabelVerticalOffset.toPx(),
                textMeasurer = textMeasurer,
            )

            val xOffset = center.x - (normalizedHeading * pixelsPerDegree)

            translate(left = xOffset) {
                drawCompassStrip(
                    params = params,
                    pixelsPerDegree = pixelsPerDegree,
                    xOffset = xOffset,
                    canvasWidth = size.width,
                    tickCenterY = center.y,
                )
            }
        }

        // Canvas 2: Fixed Lubber Line
        Canvas(modifier = Modifier.matchParentSize()) {
            val lubberLineVisualPadding = stripHeight.toPx() * 0.05f
            val currentLubberLineStrokeWidthPx = lubberLineStrokeWidth.toPx()
            drawLine(
                color = lubberLineColor,
                start = Offset(x = center.x, y = lubberLineVisualPadding),
                end = Offset(x = center.x, y = size.height - lubberLineVisualPadding),
                strokeWidth = currentLubberLineStrokeWidthPx,
            )
        }
    }
}

private data class CompassStripParams(
    val tickColor: Color,
    val majorTickHeightPx: Float,
    val minorTickHeightPx: Float,
    val majorTickStrokeWidthPx: Float,
    val minorTickStrokeWidthPx: Float,
    val showCardinalLabels: Boolean,
    val measuredCardinalLabels: Map<Int, TextLayoutResult>,
    val cardinalLabelVerticalOffsetPx: Float,
    val showDegreeLabels: Boolean,
    val degreeLabelInterval: Int,
    val degreeLabelTextStyle: TextStyle,
    val degreeLabelVerticalOffsetPx: Float,
    val textMeasurer: TextMeasurer,
)

private fun DrawScope.drawCompassStrip(
    params: CompassStripParams,
    pixelsPerDegree: Float,
    xOffset: Float,
    canvasWidth: Float,
    tickCenterY: Float,
) {
    for (repetition in -1..1) {
        val repetitionBaseDegree = repetition * 360
        for (degreeInRepetition in 0 until 360) {
            val absoluteDegree = repetitionBaseDegree + degreeInRepetition
            val xPos = absoluteDegree * pixelsPerDegree

            if (xPos < -xOffset + 2 * canvasWidth && xPos > -xOffset - canvasWidth) {
                drawDegreeTick(
                    degree = degreeInRepetition,
                    xPos = xPos,
                    tickCenterY = tickCenterY,
                    params = params,
                )
            }
        }
    }
}

private fun DrawScope.drawDegreeTick(
    degree: Int,
    xPos: Float,
    tickCenterY: Float,
    params: CompassStripParams,
) {
    val isMajorTickEquivalent = degree % 10 == 0
    val isMinorTickEquivalent = degree % 5 == 0 && !isMajorTickEquivalent

    if (isMajorTickEquivalent) {
        val tickTopY = tickCenterY - params.majorTickHeightPx / 2f
        val tickBottomY = tickCenterY + params.majorTickHeightPx / 2f
        drawLine(
            color = params.tickColor,
            start = Offset(x = xPos, y = tickTopY),
            end = Offset(x = xPos, y = tickBottomY),
            strokeWidth = params.majorTickStrokeWidthPx,
        )

        if (params.showCardinalLabels && params.measuredCardinalLabels.containsKey(degree)) {
            val measuredText = params.measuredCardinalLabels.getValue(degree)
            drawCardinalLabel(
                xPos = xPos,
                tickTopY = tickTopY,
                measuredText = measuredText,
                verticalOffsetPx = params.cardinalLabelVerticalOffsetPx,
            )
        }
    } else if (isMinorTickEquivalent) {
        val tickTopY = tickCenterY - params.minorTickHeightPx / 2f
        val tickBottomY = tickCenterY + params.minorTickHeightPx / 2f
        drawLine(
            color = params.tickColor,
            start = Offset(x = xPos, y = tickTopY),
            end = Offset(x = xPos, y = tickBottomY),
            strokeWidth = params.minorTickStrokeWidthPx,
        )
    }

    if (params.showDegreeLabels && degree % params.degreeLabelInterval == 0) {
        val tickHeightPx = when {
            isMajorTickEquivalent -> params.majorTickHeightPx
            isMinorTickEquivalent -> params.minorTickHeightPx
            else -> 0f
        }
        val tickBottomY = tickCenterY + tickHeightPx / 2f
        val labelText = degree.toString()
        val measuredText = params.textMeasurer.measure(
            labelText,
            style = params.degreeLabelTextStyle,
        )
        drawDegreeLabel(
            xPos = xPos,
            tickBottomY = tickBottomY,
            measuredText = measuredText,
            verticalOffsetPx = params.degreeLabelVerticalOffsetPx,
        )
    }
}

private fun DrawScope.drawCardinalLabel(
    xPos: Float,
    tickTopY: Float,
    measuredText: TextLayoutResult,
    verticalOffsetPx: Float,
) {
    val labelTopY = tickTopY - measuredText.size.height - verticalOffsetPx
    drawText(
        textLayoutResult = measuredText,
        topLeft = Offset(
            x = xPos - measuredText.size.width / 2f,
            y = labelTopY,
        ),
    )
}

private fun DrawScope.drawDegreeLabel(
    xPos: Float,
    tickBottomY: Float,
    measuredText: TextLayoutResult,
    verticalOffsetPx: Float,
) {
    val labelTopY = tickBottomY + verticalOffsetPx
    drawText(
        textLayoutResult = measuredText,
        topLeft = Offset(
            x = xPos - measuredText.size.width / 2f,
            y = labelTopY,
        ),
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF222222)
@Composable
private fun FlatWhiskeyCompassPreview() {
    val exampleHeadings = listOf(
        0f,
        10f,
        22.5f,
        45f,
        168f,
        270f,
        358f,
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.DarkGray)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            "Default Flat Compass Strip",
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
        )
        WhiskeyCompass(
            heading = 45f,
            modifier = Modifier.fillMaxWidth(),
            stripHeight = 100.dp,
            pixelsPerDegree = 8f,
        )

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Customized Labels & Ticks",
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
        )
        WhiskeyCompass(
            heading = 123f,
            modifier = Modifier.fillMaxWidth(),
            stripHeight = 120.dp,
            backgroundColor = Color(0xFF1A237E),
            tickColor = Color(0xFFB0BEC5),
            lubberLineColor = Color(0xFFFFD600),
            pixelsPerDegree = 12f,
            degreeLabelInterval = 10,
            degreeLabelTextStyle = MaterialTheme.typography.bodySmall.copy(
                color = Color(0xFF81D4FA),
            ),
            cardinalLabelTextStyle = MaterialTheme.typography.labelLarge.copy(
                color = Color.White,
                fontWeight = FontWeight.Bold,
            ),
            majorTickHeight = 30.dp,
            minorTickHeight = 18.dp,
            degreeLabelVerticalOffset = 6.dp,
            cardinalLabelVerticalOffset = 6.dp,
            majorTickStrokeWidth = 2.5.dp,
        )

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "No Cardinal Labels",
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
        )
        WhiskeyCompass(
            heading = 210f,
            modifier = Modifier.fillMaxWidth(),
            stripHeight = 70.dp,
            showCardinalLabels = false,
            pixelsPerDegree = 6f,
        )

        Spacer(modifier = Modifier.height(16.dp))
        Text("No Degree Labels", color = Color.White, style = MaterialTheme.typography.titleMedium)
        WhiskeyCompass(
            heading = 300f,
            modifier = Modifier.fillMaxWidth(),
            stripHeight = 70.dp,
            showDegreeLabels = false,
            pixelsPerDegree = 6f,
        )

        exampleHeadings.forEach { currentHeading ->
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Test Heading: ${currentHeading.roundToInt()}°",
                color = Color.LightGray,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
            )
            WhiskeyCompass(
                heading = currentHeading,
                modifier = Modifier.fillMaxWidth(),
                stripHeight = 90.dp,
                pixelsPerDegree = 7f,
                degreeLabelInterval = 30,
            )
        }
    }
}
