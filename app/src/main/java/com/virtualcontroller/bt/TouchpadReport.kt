package com.virtualcontroller.bt

/** Single-contact absolute HID touchpad coordinates, separate from gamepad axes. */
data class TouchpadReport(val touching: Boolean = false, val x: Int = 0, val y: Int = 0) {
    fun toByteArray(): ByteArray {
        val boundedX = x.coerceIn(0, MAX_X)
        val boundedY = y.coerceIn(0, MAX_Y)
        return byteArrayOf(
            (if (touching) 0x07 else 0).toByte(), // Tip switch, in range, confidence
            0, // Contact identifier
            (boundedX and 0xFF).toByte(), (boundedX shr 8).toByte(),
            (boundedY and 0xFF).toByte(), (boundedY shr 8).toByte(),
            (if (touching) 1 else 0).toByte() // Contact count
        )
    }

    companion object {
        const val MAX_X = 1919
        const val MAX_Y = 1079
    }
}
