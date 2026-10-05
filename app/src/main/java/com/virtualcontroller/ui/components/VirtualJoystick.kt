package com.virtualcontroller.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.virtualcontroller.bt.GamepadReport
import kotlin.math.min

@Composable
fun VirtualJoystick(
    modifier: Modifier = Modifier,
    label: String = "L3",
    opacity: Float = 0.85f,
    onPositionChanged: (x: Int, y: Int, isL3R3Pressed: Boolean) -> Unit
) {
    var thumbOffset by remember { mutableStateOf(Offset.Zero) }
    var isPressed by remember { mutableStateOf(false) }
    val sendPosition by rememberUpdatedState(onPositionChanged)

    val accentColor = if (isPressed) Color(0xFF00E5FF) else Color(0xFF00897B)
    val ringColor = Color.White.copy(alpha = 0.25f * opacity)

    Box(
        modifier = modifier
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    down.consume()
                    isPressed = true
                    try {
                        var position = ControllerTouchMath.stick(down.position.x, down.position.y, size.width.toFloat(), size.height.toFloat())
                        thumbOffset = Offset(position.offsetX, position.offsetY)
                        sendPosition(position.x, position.y, position.edgeClick)
                        while (true) {
                            val change = awaitPointerEvent().changes.firstOrNull { it.id == down.id } ?: break
                            change.consume()
                            if (!change.pressed) break
                            position = ControllerTouchMath.stick(change.position.x, change.position.y, size.width.toFloat(), size.height.toFloat())
                            thumbOffset = Offset(position.offsetX, position.offsetY)
                            sendPosition(position.x, position.y, position.edgeClick)
                        }
                    } finally {
                        isPressed = false
                        thumbOffset = Offset.Zero
                        sendPosition(GamepadReport.CENTER_AXIS, GamepadReport.CENTER_AXIS, false)
                    }
                }
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
