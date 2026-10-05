package com.virtualcontroller.bt

import org.junit.Assert.assertEquals
import org.junit.Test

class GamepadReportTest {

    @Test
    fun testDefaultReportSerialization() {
        val report = GamepadReport()
        val bytes = report.toByteArray()

        assertEquals(10, bytes.size)
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

    private fun hostButtons(report: GamepadReport): Int {
        val bytes = report.toByteArray()
        return (bytes[7].toInt() and 255) or ((bytes[8].toInt() and 255) shl 8) or ((bytes[9].toInt() and 255) shl 16)
    }

    @Test
    fun triggersAndStickClicksUseSeparateHostButtons() {
        assertEquals(1 shl 6, hostButtons(GamepadReport(l2Trigger = 255)))
        assertEquals(1 shl 7, hostButtons(GamepadReport(r2Trigger = 255)))
        assertEquals(1 shl 10, hostButtons(GamepadReport(buttonsMask = GamepadReport.BUTTON_L3)))
        assertEquals(1 shl 11, hostButtons(GamepadReport(buttonsMask = GamepadReport.BUTTON_R3)))
        assertEquals(1 shl 17, hostButtons(GamepadReport(buttonsMask = GamepadReport.BUTTON_TOUCHPAD)))
        assertEquals(0, hostButtons(GamepadReport()))
    }

    @Test
    fun allHatDirectionsAlsoProduceDigitalDirectionsAndRelease() {
        val expected = listOf(0x1000, 0x9000, 0x8000, 0xA000, 0x2000, 0x6000, 0x4000, 0x5000, 0)
        expected.forEachIndexed { hat, mask ->
            assertEquals(mask, hostButtons(GamepadReport(dPadHat = hat)))
            assertEquals(hat.toByte(), GamepadReport(dPadHat = hat).toByteArray()[6])
        }
        assertEquals(0, hostButtons(GamepadReport(dPadHat = -1)))
    }
}
