package com.virtualcontroller.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.virtualcontroller.bt.GamepadReport
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun VirtualJoystick(
    modifier: Modifier = Modifier,
    label: String = "L3",
    opacity: Float = 0.85f,
    onPositionChanged: (x: Int, y: Int, isL3R3Pressed: Boolean) -> Unit
) {
    var thumbOffset by remember { mutableStateOf(Offset.Zero) }
    var isPressed by remember { mutableStateOf(false) }

    val accentColor = if (isPressed) Color(0xFF00E5FF) else Color(0xFF00897B)
    val ringColor = Color.White.copy(alpha = 0.25f * opacity)

    Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        isPressed = true
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val maxRadius = min(size.width, size.height) / 2f
                        val delta = offset - center
                        val dist = sqrt(delta.x * delta.x + delta.y * delta.y)

                        thumbOffset = if (dist > maxRadius) {
                            val angle = atan2(delta.y, delta.x)
                            Offset(cos(angle) * maxRadius, sin(angle) * maxRadius)
                        } else {
                            delta
                        }
                        dispatchPosition(thumbOffset, maxRadius, true, onPositionChanged)
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val maxRadius = min(size.width, size.height) / 2f
                        val newOffset = thumbOffset + dragAmount
                        val dist = sqrt(newOffset.x * newOffset.x + newOffset.y * newOffset.y)

                        thumbOffset = if (dist > maxRadius) {
                            val angle = atan2(newOffset.y, newOffset.x)
                            Offset(cos(angle) * maxRadius, sin(angle) * maxRadius)
                        } else {
                            newOffset
                        }
                        dispatchPosition(thumbOffset, maxRadius, true, onPositionChanged)
                    },
                    onDragEnd = {
                        isPressed = false
                        thumbOffset = Offset.Zero
                        dispatchPosition(Offset.Zero, 1f, false, onPositionChanged)
                    },
                    onDragCancel = {
                        isPressed = false
                        thumbOffset = Offset.Zero
                        dispatchPosition(Offset.Zero, 1f, false, onPositionChanged)
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val outerRadius = min(size.width, size.height) / 2f
            val thumbRadius = outerRadius * 0.40f

            // Outer Base Ring
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF1E293B).copy(alpha = 0.6f * opacity), Color(0xFF0F172A).copy(alpha = 0.9f * opacity)),
                    center = center,
                    radius = outerRadius
                ),
                radius = outerRadius,
                center = center
            )
            drawCircle(
                color = ringColor,
                radius = outerRadius,
                center = center,
                style = Stroke(width = 3.dp.toPx())
            )

            // Inner Thumbstick Cap
            val thumbCenter = center + thumbOffset
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(accentColor.copy(alpha = opacity), Color(0xFF004D40).copy(alpha = opacity)),
                    center = thumbCenter,
                    radius = thumbRadius
                ),
                radius = thumbRadius,
                center = thumbCenter
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.8f * opacity),
                radius = thumbRadius,
                center = thumbCenter,
                style = Stroke(width = 2.dp.toPx())
            )
        }
    }
}

private fun dispatchPosition(
    offset: Offset,
    maxRadius: Float,
    isPressed: Boolean,
    onPositionChanged: (x: Int, y: Int, isClick: Boolean) -> Unit
) {
    if (maxRadius <= 0f) return
    val normX = (offset.x / maxRadius).coerceIn(-1.0f, 1.0f)
    val normY = (offset.y / maxRadius).coerceIn(-1.0f, 1.0f)

    val hidX = (128 + normX * 127).toInt().coerceIn(0, 255)
    val hidY = (128 + normY * 127).toInt().coerceIn(0, 255)

    onPositionChanged(hidX, hidY, isPressed && (normX * normX + normY * normY > 0.85f))
}
