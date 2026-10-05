package com.virtualcontroller.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.virtualcontroller.bt.GamepadReport
import kotlin.math.atan2
import kotlin.math.sqrt

@Composable
fun VirtualDPad(
    modifier: Modifier = Modifier,
    opacity: Float = 0.85f,
    onDirectionChanged: (hatValue: Int) -> Unit
) {
    var activeDirection by remember { mutableStateOf(GamepadReport.DPAD_RELEASED) }

    Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { offset ->
                        activeDirection = calculateDirection(offset, size.width.toFloat(), size.height.toFloat())
                        onDirectionChanged(activeDirection)
                        tryAwaitRelease()
                        activeDirection = GamepadReport.DPAD_RELEASED
                        onDirectionChanged(activeDirection)
                    }
                )
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        activeDirection = calculateDirection(offset, size.width.toFloat(), size.height.toFloat())
                        onDirectionChanged(activeDirection)
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        activeDirection = calculateDirection(change.position, size.width.toFloat(), size.height.toFloat())
                        onDirectionChanged(activeDirection)
                    },
                    onDragEnd = {
                        activeDirection = GamepadReport.DPAD_RELEASED
                        onDirectionChanged(activeDirection)
                    },
                    onDragCancel = {
                        activeDirection = GamepadReport.DPAD_RELEASED
                        onDirectionChanged(activeDirection)
                    }
                )
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
                        GamepadReport.DPAD_UP -> {
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

private fun calculateDirection(touch: Offset, width: Float, height: Float): Int {
    val cx = width / 2f
    val cy = height / 2f
    val dx = touch.x - cx
    val dy = touch.y - cy
    val dist = sqrt(dx * dx + dy * dy)

    if (dist < width * 0.08f) return GamepadReport.DPAD_RELEASED

    var angleDeg = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
    if (angleDeg < 0) angleDeg += 360f

    return when {
        angleDeg >= 337.5f || angleDeg < 22.5f -> GamepadReport.DPAD_RIGHT
        angleDeg >= 22.5f && angleDeg < 67.5f -> GamepadReport.DPAD_DOWN_RIGHT
        angleDeg >= 67.5f && angleDeg < 112.5f -> GamepadReport.DPAD_DOWN
        angleDeg >= 112.5f && angleDeg < 157.5f -> GamepadReport.DPAD_DOWN_LEFT
        angleDeg >= 157.5f && angleDeg < 202.5f -> GamepadReport.DPAD_LEFT
        angleDeg >= 202.5f && angleDeg < 247.5f -> GamepadReport.DPAD_UP_LEFT
        angleDeg >= 247.5f && angleDeg < 292.5f -> GamepadReport.DPAD_UP
        angleDeg >= 292.5f && angleDeg < 337.5f -> GamepadReport.DPAD_UP_RIGHT
        else -> GamepadReport.DPAD_RELEASED
    }
}
