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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.virtualcontroller.bt.GamepadReport

@Composable
fun VirtualDPad(
    modifier: Modifier = Modifier,
    opacity: Float = 0.85f,
    onDirectionChanged: (hatValue: Int) -> Unit
) {
    var activeDirection by remember { mutableStateOf(GamepadReport.DPAD_RELEASED) }
    val sendDirection by rememberUpdatedState(onDirectionChanged)

    Box(
        modifier = modifier
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    down.consume()
                    try {
                        activeDirection = ControllerTouchMath.dPad(down.position.x, down.position.y, size.width.toFloat(), size.height.toFloat())
                        sendDirection(activeDirection)
                        while (true) {
                            val change = awaitPointerEvent().changes.firstOrNull { it.id == down.id } ?: break
                            change.consume()
                            if (!change.pressed) break
                            val direction = ControllerTouchMath.dPad(change.position.x, change.position.y, size.width.toFloat(), size.height.toFloat())
                            if (direction != activeDirection) {
                                activeDirection = direction
                                sendDirection(direction)
                            }
                        }
                    } finally {
                        activeDirection = GamepadReport.DPAD_RELEASED
                        sendDirection(activeDirection)
                    }
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val cx = w / 2f
            val cy = h / 2f
            val armWidth = w * 0.32f
            val armLength = w * 0.48f

            val baseColor = Color(0xFF1E293B).copy(alpha = 0.85f * opacity)
            val highlightColor = Color(0xFF00E5FF).copy(alpha = opacity)

            // Draw Cross D-Pad Body
            val path = Path().apply {
                // Top Arm
                moveTo(cx - armWidth / 2f, cy - armLength)
                lineTo(cx + armWidth / 2f, cy - armLength)
                lineTo(cx + armWidth / 2f, cy - armWidth / 2f)
                // Right Arm
                lineTo(cx + armLength, cy - armWidth / 2f)
                lineTo(cx + armLength, cy + armWidth / 2f)
                lineTo(cx + armWidth / 2f, cy + armWidth / 2f)
                // Bottom Arm
                lineTo(cx + armWidth / 2f, cy + armLength)
                lineTo(cx - armWidth / 2f, cy + armLength)
                lineTo(cx - armWidth / 2f, cy + armWidth / 2f)
                // Left Arm
                lineTo(cx - armLength, cy + armWidth / 2f)
                lineTo(cx - armLength, cy - armWidth / 2f)
                lineTo(cx - armWidth / 2f, cy - armWidth / 2f)
                close()
            }

            drawPath(path, color = baseColor)
            drawPath(path, color = Color.White.copy(alpha = 0.3f * opacity), style = Stroke(width = 2.dp.toPx()))

            // Highlight Active Arm
            if (activeDirection != GamepadReport.DPAD_RELEASED) {
                val activePath = Path().apply {
                    when (activeDirection) {
                        GamepadReport.DPAD_UP, GamepadReport.DPAD_UP_LEFT, GamepadReport.DPAD_UP_RIGHT -> {
                            moveTo(cx - armWidth / 2f, cy - armLength)
                            lineTo(cx + armWidth / 2f, cy - armLength)
                            lineTo(cx + armWidth / 2f, cy - armWidth / 2f)
                            lineTo(cx - armWidth / 2f, cy - armWidth / 2f)
                            close()
                        }
                        GamepadReport.DPAD_RIGHT -> {
                            moveTo(cx + armWidth / 2f, cy - armWidth / 2f)
                            lineTo(cx + armLength, cy - armWidth / 2f)
                            lineTo(cx + armLength, cy + armWidth / 2f)
                            lineTo(cx + armWidth / 2f, cy + armWidth / 2f)
                            close()
                        }
                        GamepadReport.DPAD_DOWN -> {
                            moveTo(cx - armWidth / 2f, cy + armWidth / 2f)
                            lineTo(cx + armWidth / 2f, cy + armWidth / 2f)
                            lineTo(cx + armWidth / 2f, cy + armLength)
                            lineTo(cx - armWidth / 2f, cy + armLength)
                            close()
                        }
                        GamepadReport.DPAD_LEFT -> {
                            moveTo(cx - armLength, cy - armWidth / 2f)
                            lineTo(cx - armWidth / 2f, cy - armWidth / 2f)
                            lineTo(cx - armWidth / 2f, cy + armWidth / 2f)
                            lineTo(cx - armLength, cy + armWidth / 2f)
                            close()
                        }
                    }
                }
                drawPath(activePath, color = highlightColor)
            }
        }
    }
}
