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
     * Serializes input state into a 10-byte HID report payload.
     * Keep the app's logical button bits stable for saved layouts, and translate
     * them to the host's common button order only at the wire boundary.
     */
    fun toByteArray(): ByteArray {
        val hatValue = if (dPadHat in 0..7) dPadHat else 8 // 8 represents neutral/released in HID Hat Switch
        var hostButtons = buttonsMask and 0x3F // Face buttons and bumpers
        if (l2Trigger > 0) hostButtons = hostButtons or (1 shl 6)
        if (r2Trigger > 0) hostButtons = hostButtons or (1 shl 7)
        if (buttonsMask and BUTTON_SELECT != 0) hostButtons = hostButtons or (1 shl 8)
        if (buttonsMask and BUTTON_START != 0) hostButtons = hostButtons or (1 shl 9)
        if (buttonsMask and BUTTON_L3 != 0) hostButtons = hostButtons or (1 shl 10)
        if (buttonsMask and BUTTON_R3 != 0) hostButtons = hostButtons or (1 shl 11)
        if (hatValue == 0 || hatValue == 1 || hatValue == 7) hostButtons = hostButtons or (1 shl 12)
        if (hatValue in 3..5) hostButtons = hostButtons or (1 shl 13)
        if (hatValue in 5..7) hostButtons = hostButtons or (1 shl 14)
        if (hatValue in 1..3) hostButtons = hostButtons or (1 shl 15)
        if (buttonsMask and BUTTON_MODE != 0) hostButtons = hostButtons or (1 shl 16)
        if (buttonsMask and BUTTON_TOUCHPAD != 0) hostButtons = hostButtons or (1 shl 17)
        if (buttonsMask and BUTTON_SHARE != 0) hostButtons = hostButtons or (1 shl 18)
        return byteArrayOf(
            leftStickX.clampToByte(),
            leftStickY.clampToByte(),
            rightStickX.clampToByte(),
            rightStickY.clampToByte(),
            l2Trigger.clampToByte(),
            r2Trigger.clampToByte(),
            (hatValue and 0x0F).toByte(),
            (hostButtons and 0xFF).toByte(),
            ((hostButtons shr 8) and 0xFF).toByte(),
            ((hostButtons shr 16) and 0xFF).toByte()
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
