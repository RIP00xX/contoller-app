package com.virtualcontroller.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun VirtualTriggerSlider(
    modifier: Modifier = Modifier,
    label: String = "L2 Trigger",
    opacity: Float = 0.85f,
    accentColor: Color = Color(0xFFFF5252),
    onPressureChanged: (value: Int) -> Unit
) {
    var pressureValue by remember { mutableStateOf(0) }

    Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        pressureValue = calculatePressure(offset.y, size.height.toFloat())
                        onPressureChanged(pressureValue)
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        pressureValue = calculatePressure(change.position.y, size.height.toFloat())
                        onPressureChanged(pressureValue)
                    },
                    onDragEnd = {
                        pressureValue = 0
                        onPressureChanged(0)
                    },
                    onDragCancel = {
                        pressureValue = 0
                        onPressureChanged(0)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val fillHeight = size.height * (pressureValue / 255f)

            // Background Bar
            drawRoundRect(
                color = Color(0xFF1E293B).copy(alpha = 0.85f * opacity),
                cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx())
            )

            // Dynamic Fill Level
            if (pressureValue > 0) {
                drawRoundRect(
                    color = accentColor.copy(alpha = opacity),
                    topLeft = Offset(0f, size.height - fillHeight),
                    size = Size(size.width, fillHeight),
                    cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx())
                )
            }
        }

        Text(
            text = "$label\n${(pressureValue / 2.55f).toInt()}%",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}

private fun calculatePressure(yPos: Float, height: Float): Int {
    if (height <= 0f) return 0
    val normalized = (1.0f - (yPos / height)).coerceIn(0.0f, 1.0f)
    return (normalized * 255).toInt()
}
