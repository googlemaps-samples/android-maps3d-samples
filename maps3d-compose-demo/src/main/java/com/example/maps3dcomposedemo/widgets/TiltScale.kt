package com.example.maps3dcomposedemo.widgets

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TiltScale(tilt: Float, modifier: Modifier = Modifier) {
    // User requested: 90 = straight down, 0 = horizontal
    // SDK: 0 = straight down, 90 = horizontal
    // So displayedTilt = 90 - sdkTilt
    val displayedTilt = 90f - tilt
    val textMeasurer = rememberTextMeasurer()
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimaryContainer

    Box(
        modifier = modifier
            .height(300.dp)
            .width(80.dp)
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f))
            .clipToBounds(),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerLineY = size.height / 2f
            val centerLineX = size.width / 2f
            
            // Pixels per degree
            val pixelsPerDegree = 5f
            
            // Draw a fixed center line (pointer)
            drawLine(
                color = Color.Red,
                start = Offset(0f, centerLineY),
                end = Offset(size.width, centerLineY),
                strokeWidth = 2f
            )

            // Draw scrolling ticks
            translate(top = centerLineY + (displayedTilt * pixelsPerDegree)) {
                for (i in 0..90 step 5) {
                    val yPos = -i * pixelsPerDegree
                    
                    val isMajor = i % 15 == 0
                    val tickLength = if (isMajor) 15f else 8f
                    
                    drawLine(
                        color = onPrimaryColor,
                        start = Offset(centerLineX - tickLength, yPos),
                        end = Offset(centerLineX + tickLength, yPos),
                        strokeWidth = if (isMajor) 2f else 1f
                    )
                    
                    if (isMajor) {
                        val measuredText = textMeasurer.measure(i.toString(), style = TextStyle(color = onPrimaryColor, fontSize = 12.sp))
                        drawText(
                            textLayoutResult = measuredText,
                            topLeft = Offset(centerLineX + 20f, yPos - measuredText.size.height / 2f)
                        )
                    }
                }
            }
        }
    }
}
