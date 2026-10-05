package com.virtualcontroller.ui.components

import com.virtualcontroller.bt.GamepadReport
import kotlin.math.atan2
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sqrt

data class StickPosition(val x: Int, val y: Int, val offsetX: Float, val offsetY: Float, val edgeClick: Boolean)

object ControllerTouchMath {
    fun stick(touchX: Float, touchY: Float, width: Float, height: Float): StickPosition {
        val radius = min(width, height) / 2f
        if (radius <= 0f) return StickPosition(128, 128, 0f, 0f, false)
        val dx = touchX - width / 2f
        val dy = touchY - height / 2f
        val length = sqrt(dx * dx + dy * dy)
        val scale = if (length > radius) radius / length else 1f
        val ox = dx * scale
        val oy = dy * scale
        val nx = ox / radius
        val ny = oy / radius
        return StickPosition(
            ((nx + 1f) * 127.5f).roundToInt().coerceIn(0, 255),
            ((ny + 1f) * 127.5f).roundToInt().coerceIn(0, 255),
            ox, oy, nx * nx + ny * ny > 0.85f
        )
    }

    fun dPad(touchX: Float, touchY: Float, width: Float, height: Float): Int {
        if (width <= 0f || height <= 0f) return GamepadReport.DPAD_RELEASED
        val dx = touchX - width / 2f
        val dy = touchY - height / 2f
        if (sqrt(dx * dx + dy * dy) < min(width, height) * 0.08f) return GamepadReport.DPAD_RELEASED
        val degrees = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble()))
        // Clockwise sectors beginning at Up; screen Y grows downwards.
        return (((degrees + 90.0 + 360.0 + 22.5) / 45.0).toInt() % 8)
    }
}
