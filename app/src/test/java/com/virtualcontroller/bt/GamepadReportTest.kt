package com.virtualcontroller.bt

import org.junit.Assert.assertEquals
import org.junit.Test

class GamepadReportTest {

    @Test
    fun testDefaultReportSerialization() {
        val report = GamepadReport()
        val bytes = report.toByteArray()

        assertEquals(9, bytes.size)
        assertEquals(128.toByte(), bytes[0]) // LX center
        assertEquals(128.toByte(), bytes[1]) // LY center
        assertEquals(128.toByte(), bytes[2]) // RX center
        assertEquals(128.toByte(), bytes[3]) // RY center
        assertEquals(0.toByte(), bytes[4])   // L2 rest
        assertEquals(0.toByte(), bytes[5])   // R2 rest
        assertEquals(8.toByte(), bytes[6])   // D-Pad Hat released
        assertEquals(0.toByte(), bytes[7])   // Buttons byte 0
        assertEquals(0.toByte(), bytes[8])   // Buttons byte 1
    }

    @Test
    fun testButtonPressedSerialization() {
        val report = GamepadReport(
            buttonsMask = GamepadReport.BUTTON_A or GamepadReport.BUTTON_START
        )
        val bytes = report.toByteArray()

        assertEquals((GamepadReport.BUTTON_A and 0xFF).toByte(), bytes[7])
        assertEquals(((GamepadReport.BUTTON_START shr 8) and 0xFF).toByte(), bytes[8])
    }
}
