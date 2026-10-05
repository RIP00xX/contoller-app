package com.virtualcontroller.bt

/**
 * Gamepad input state model that serializes to HID input report byte array.
 */
data class GamepadReport(
    val leftStickX: Int = CENTER_AXIS,   // 0..255
    val leftStickY: Int = CENTER_AXIS,   // 0..255
    val rightStickX: Int = CENTER_AXIS,  // 0..255
    val rightStickY: Int = CENTER_AXIS,  // 0..255
    val l2Trigger: Int = MIN_AXIS,       // 0..255
    val r2Trigger: Int = MIN_AXIS,       // 0..255
    val dPadHat: Int = DPAD_RELEASED,    // 0..7, 8=RELEASED
    val buttonsMask: Int = 0             // Bitmask of pressed buttons (0 to 65535)
) {

    /**
     * Serializes input state into exact 9-byte HID report payload.
     */
    fun toByteArray(): ByteArray {
        val hatValue = if (dPadHat in 0..7) dPadHat else 8 // 8 represents neutral/released in HID Hat Switch
        return byteArrayOf(
            leftStickX.clampToByte(),
            leftStickY.clampToByte(),
            rightStickX.clampToByte(),
            rightStickY.clampToByte(),
            l2Trigger.clampToByte(),
            r2Trigger.clampToByte(),
            (hatValue and 0x0F).toByte(),
            (buttonsMask and 0xFF).toByte(),
            ((buttonsMask shr 8) and 0xFF).toByte()
        )
    }

    private fun Int.clampToByte(): Byte = coerceIn(0, 255).toByte()

    companion object {
        const val CENTER_AXIS = 128
        const val MIN_AXIS = 0
        const val MAX_AXIS = 255

        const val DPAD_UP = 0
        const val DPAD_UP_RIGHT = 1
        const val DPAD_RIGHT = 2
        const val DPAD_DOWN_RIGHT = 3
        const val DPAD_DOWN = 4
        const val DPAD_DOWN_LEFT = 5
        const val DPAD_LEFT = 6
        const val DPAD_UP_LEFT = 7
        const val DPAD_RELEASED = 8

        // Button Bit Positions
        const val BUTTON_A = 1 shl 0
        const val BUTTON_B = 1 shl 1
        const val BUTTON_X = 1 shl 2
        const val BUTTON_Y = 1 shl 3
        const val BUTTON_L1 = 1 shl 4
        const val BUTTON_R1 = 1 shl 5
        const val BUTTON_L3 = 1 shl 6
        const val BUTTON_R3 = 1 shl 7
        const val BUTTON_SELECT = 1 shl 8
        const val BUTTON_START = 1 shl 9
        const val BUTTON_MODE = 1 shl 10
        const val BUTTON_SHARE = 1 shl 11
        const val BUTTON_TOUCHPAD = 1 shl 12
    }
}
