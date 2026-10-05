package com.virtualcontroller.haptics

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class HapticFeedbackManager(context: Context) {

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    /**
     * Performs a short tactile button click haptic pulse.
     * @param intensity Float value between 0.0 (off) and 1.0 (maximum)
     */
    fun performClickHaptic(intensity: Float) {
        if (intensity <= 0.0f || vibrator == null || !vibrator.hasVibrator()) return

        val amplitude = (intensity * 255).toInt().coerceIn(1, 255)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = VibrationEffect.createOneShot(15, amplitude)
            vibrator.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(15)
        }
    }

    /**
     * Performs a light tick pulse for joystick movement or subtle feedback.
     */
    fun performTickHaptic(intensity: Float) {
        if (intensity <= 0.0f || vibrator == null || !vibrator.hasVibrator()) return

        val amplitude = (intensity * 180).toInt().coerceIn(1, 255)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val effect = VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
            vibrator.vibrate(effect)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = VibrationEffect.createOneShot(8, amplitude)
            vibrator.vibrate(effect)
        }
    }

    /**
     * Performs dynamic haptic feedback based on analog trigger pressure.
     */
    fun performTriggerHaptic(pressureNormalized: Float, intensitySetting: Float) {
        if (intensitySetting <= 0.0f || vibrator == null || !vibrator.hasVibrator()) return
        if (pressureNormalized <= 0.05f) return

        val amplitude = (pressureNormalized * intensitySetting * 255).toInt().coerceIn(1, 255)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = VibrationEffect.createOneShot(10, amplitude)
            vibrator.vibrate(effect)
        }
    }
}
