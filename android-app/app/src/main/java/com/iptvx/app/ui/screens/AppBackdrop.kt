package com.iptvx.app.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke

@Composable
fun AppBackdrop(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF111820),
                    Color(0xFF070B10),
                    Color(0xFF030507)
                ),
                startY = 0f,
                endY = size.height
            )
        )

        val cyanBand = Path().apply {
            moveTo(-size.width * 0.10f, size.height * 0.10f)
            lineTo(size.width * 0.26f, 0f)
            lineTo(size.width * 0.74f, size.height)
            lineTo(size.width * 0.44f, size.height)
            close()
        }
        drawPath(
            path = cyanBand,
            brush = Brush.linearGradient(
                colors = listOf(Color.Transparent, Color(0x3310E0A0), Color.Transparent),
                start = Offset(0f, 0f),
                end = Offset(size.width, size.height)
            )
        )

        val blueBand = Path().apply {
            moveTo(size.width * 0.45f, 0f)
            lineTo(size.width * 0.72f, 0f)
            lineTo(size.width * 1.05f, size.height)
            lineTo(size.width * 0.78f, size.height)
            close()
        }
        drawPath(
            path = blueBand,
            brush = Brush.linearGradient(
                colors = listOf(Color.Transparent, Color(0x332E7BFF), Color(0x2245D4FF)),
                start = Offset(size.width * 0.45f, 0f),
                end = Offset(size.width, size.height)
            )
        )

        val goldBand = Path().apply {
            moveTo(size.width * 0.70f, 0f)
            lineTo(size.width * 0.86f, 0f)
            lineTo(size.width * 0.28f, size.height)
            lineTo(size.width * 0.12f, size.height)
            close()
        }
        drawPath(
            path = goldBand,
            brush = Brush.linearGradient(
                colors = listOf(Color.Transparent, Color(0x22F4D21B), Color.Transparent),
                start = Offset(size.width, 0f),
                end = Offset(0f, size.height)
            )
        )

        repeat(9) { index ->
            val y = size.height * (0.16f + index * 0.075f)
            drawLine(
                color = Color.White.copy(alpha = 0.022f),
                start = Offset(-size.width * 0.08f, y),
                end = Offset(size.width * 1.04f, y + size.height * 0.22f),
                strokeWidth = 1.2f
            )
        }

        drawLine(
            brush = Brush.horizontalGradient(
                colors = listOf(Color.Transparent, Color(0x66F4D21B), Color.Transparent)
            ),
            start = Offset(0f, size.height - 2f),
            end = Offset(size.width, size.height - 2f),
            strokeWidth = 3f
        )

        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(Color.Transparent, Color(0x99000000)),
                center = Offset(size.width * 0.50f, size.height * 0.52f),
                radius = size.maxDimension * 0.74f
            ),
            style = Stroke(width = size.maxDimension * 0.32f)
        )
    }
}
