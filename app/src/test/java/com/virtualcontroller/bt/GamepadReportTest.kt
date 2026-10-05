package com.virtualcontroller.bt

import org.junit.Assert.assertEquals
import org.junit.Test

class GamepadReportTest {

    @Test
    fun testDefaultReportSerialization() {
        val report = GamepadReport()
        val bytes = report.toByteArray()

        assertEquals(7, bytes.size)
        assertEquals(128.toByte(), bytes[0]) // LX center
        assertEquals(128.toByte(), bytes[1]) // LY center
        assertEquals(128.toByte(), bytes[2]) // RX center
        assertEquals(128.toByte(), bytes[3]) // RY center
        assertEquals(0.toByte(), bytes[4])   // Buttons byte 0
        assertEquals(0.toByte(), bytes[5])   // Buttons byte 1
        assertEquals(0.toByte(), bytes[6])   // Buttons byte 2
    }

    @Test
    fun testButtonPressedSerialization() {
        val report = GamepadReport(
            buttonsMask = GamepadReport.BUTTON_A or GamepadReport.BUTTON_START
        )
        val bytes = report.toByteArray()

        assertEquals((GamepadReport.BUTTON_A and 0xFF).toByte(), bytes[4])
        assertEquals(((GamepadReport.BUTTON_START shr 8) and 0xFF).toByte(), bytes[5])
    }

    private fun hostButtons(report: GamepadReport): Int {
        val bytes = report.toByteArray()
        return (bytes[4].toInt() and 255) or ((bytes[5].toInt() and 255) shl 8) or ((bytes[6].toInt() and 255) shl 16)
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
    fun allDirectionsProduceDigitalButtonsAndRelease() {
        val expected = listOf(0x1000, 0x9000, 0x8000, 0xA000, 0x2000, 0x6000, 0x4000, 0x5000, 0)
        expected.forEachIndexed { hat, mask ->
            assertEquals(mask, hostButtons(GamepadReport(dPadHat = hat)))
        }
        assertEquals(0, hostButtons(GamepadReport(dPadHat = -1)))
    }

    @Test
    fun releasingDpadWhileHoldingTriggerClearsEveryDirection() {
        val held = GamepadReport(l2Trigger = 255, dPadHat = GamepadReport.DPAD_UP)
        assertEquals((1 shl 6) or (1 shl 12), hostButtons(held))
        val released = held.copy(dPadHat = GamepadReport.DPAD_RELEASED)
        assertEquals(1 shl 6, hostButtons(released))
        assertEquals(0, hostButtons(released) and 0xF000)
        assertEquals(listOf(128, 128, 128, 128), released.toByteArray().take(4).map { it.toInt() and 255 })
    }
}
