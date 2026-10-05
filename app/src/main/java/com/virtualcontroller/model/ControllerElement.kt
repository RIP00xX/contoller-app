package com.virtualcontroller.model

enum class TargetScreen {
    INNER_SCREEN,
    OUTER_SCREEN
}

enum class ControllerElementType {
    JOYSTICK_LEFT,
    JOYSTICK_RIGHT,
    DPAD,
    BUTTON_A,
    BUTTON_B,
    BUTTON_X,
    BUTTON_Y,
    BUMPER_L1,
    BUMPER_R1,
    TRIGGER_L2,
    TRIGGER_R2,
    BUTTON_START,
    BUTTON_SELECT,
    BUTTON_MODE,
    STEERING_WHEEL
}

data class ControllerElement(
    val id: String,
    val type: ControllerElementType,
    val label: String,
    val targetScreen: TargetScreen = TargetScreen.INNER_SCREEN,
    val xPercent: Float, // Normalized 0.0 to 1.0
    val yPercent: Float, // Normalized 0.0 to 1.0
    val sizePercent: Float = 0.20f, // Relative size 0.05 to 0.50
    val opacity: Float = 0.85f,     // Opacity 0.1 to 1.0
    val hapticIntensity: Float = 0.7f, // Haptic strength 0.0 to 1.0
    val mappedHidBitOrAxis: Int = 0
)
