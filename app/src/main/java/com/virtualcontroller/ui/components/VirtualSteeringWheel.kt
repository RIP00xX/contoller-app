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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.atan2
import kotlin.math.min

@Composable
fun VirtualSteeringWheel(
    modifier: Modifier = Modifier,
    opacity: Float = 0.85f,
    onSteeringAngleChanged: (hidXValue: Int) -> Unit
) {
    var rotationAngle by remember { mutableStateOf(0f) } // -90 deg (left) to +90 deg (right)

    Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val angle = Math.toDegrees(atan2((offset.y - center.y).toDouble(), (offset.x - center.x).toDouble())).toFloat()
                        rotationAngle = (angle - 90f).coerceIn(-90f, 90f)
                        onSteeringAngleChanged(angleToHid(rotationAngle))
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val angle = Math.toDegrees(atan2((change.position.y - center.y).toDouble(), (change.position.x - center.x).toDouble())).toFloat()
                        rotationAngle = (angle - 90f).coerceIn(-90f, 90f)
                        onSteeringAngleChanged(angleToHid(rotationAngle))
                    },
                    onDragEnd = {
                        rotationAngle = 0f
                        onSteeringAngleChanged(128)
                    },
                    onDragCancel = {
                        rotationAngle = 0f
                        onSteeringAngleChanged(128)
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = min(size.width, size.height) / 2f

            rotate(rotationAngle, pivot = center) {
                // Outer Rim
                drawCircle(
                    color = Color(0xFF1E293B).copy(alpha = opacity),
                    radius = radius,
                    center = center
                )
                drawCircle(
                    color = Color(0xFF00E5FF).copy(alpha = opacity),
                    radius = radius,
                    center = center,
                    style = Stroke(width = 12.dp.toPx())
                )
                // Center Hub & Spokes
                drawCircle(
                    color = Color(0xFF0F172A).copy(alpha = opacity),
                    radius = radius * 0.35f,
                    center = center
                )
                // Top Indicator Strip
                drawLine(
                    color = Color.Red,
                    start = Offset(center.x, center.y - radius),
                    end = Offset(center.x, center.y - radius + 20.dp.toPx()),
                    strokeWidth = 6.dp.toPx()
                )
            }
        }
    }
}

private fun angleToHid(angleDeg: Float): Int {
    val norm = (angleDeg / 90f).coerceIn(-1.0f, 1.0f)
    return (128 + norm * 127).toInt().coerceIn(0, 255)
}
