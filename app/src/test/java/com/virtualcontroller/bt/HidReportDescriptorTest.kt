package com.virtualcontroller.bt

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class HidReportDescriptorTest {
    @Test
    fun windowsRawAxisSlotsContainOnlyTheFourStickAxes() {
        val descriptor = HidReportDescriptor.GAMEPAD_DESCRIPTOR
        var offset = 0
        var page = 0
        val usages = mutableListOf<Int>()
        val axisUsages = mutableListOf<Int>()
        while (offset < descriptor.size) {
            val prefix = descriptor[offset++].toInt() and 255
            val length = if (prefix and 3 == 3) 4 else prefix and 3
            var value = 0
            repeat(length) { index -> value = value or ((descriptor[offset++].toInt() and 255) shl (8 * index)) }
            val type = (prefix shr 2) and 3
            val tag = prefix shr 4
            if (type == 1 && tag == 0) page = value
            if (type == 2 && tag == 0) usages.add(value)
            if (type == 0) {
                if (tag == 8 && page == 1) axisUsages.addAll(usages.filter { it in 0x30..0x35 })
                usages.clear()
            }
        }
        // Chromium's Windows raw input path indexes axes by usage - 0x30.
        val bytes = GamepadReport(rightStickX = 200, rightStickY = 220).toByteArray()
        val windowsAxes = IntArray(4)
        axisUsages.forEachIndexed { wireIndex, usage -> windowsAxes[usage - 0x30] = bytes[wireIndex].toInt() and 255 }
        assertEquals(listOf(128, 128, 200, 220), windowsAxes.toList())
        assertEquals(listOf(0x30, 0x31, 0x32, 0x33), axisUsages)
    }

    @Test
    fun reportSizesMatchWirePayloadsWithoutAnAdditionalHatAxis() {
        val descriptor = HidReportDescriptor.CONTROLLER_DESCRIPTOR
        var offset = 0
        var reportId = 0
        var size = 0
        var count = 0
        var usagePage = 0
        var lastUsage = 0
        var hasHat = false
        var collections = 0
        val inputBits = mutableMapOf<Int, Int>()
        val featureBits = mutableMapOf<Int, Int>()
        while (offset < descriptor.size) {
            val prefix = descriptor[offset++].toInt() and 0xFF
            val length = when (prefix and 3) { 3 -> 4; else -> prefix and 3 }
            var value = 0
            repeat(length) { index -> value = value or ((descriptor[offset++].toInt() and 0xFF) shl (index * 8)) }
            val type = (prefix shr 2) and 3
            val tag = prefix shr 4
            if (type == 1) {
                when (tag) {
                    0 -> usagePage = value
                    7 -> size = value
                    8 -> reportId = value
                    9 -> count = value
                }
            } else if (type == 2 && tag == 0) {
                lastUsage = value
            } else if (type == 0) {
                when (tag) {
                    8 -> {
                        inputBits[reportId] = (inputBits[reportId] ?: 0) + size * count
                        if (usagePage == 1 && lastUsage == 0x39) hasHat = true
                    }
                    10 -> collections++
                    11 -> featureBits[reportId] = (featureBits[reportId] ?: 0) + size * count
                    12 -> collections--
                }
                lastUsage = 0 // Local items do not survive a main item.
            }
        }
        assertEquals(0, collections)
        assertEquals(mapOf(1 to GamepadReport().toByteArray().size * 8, 2 to TouchpadReport().toByteArray().size * 8), inputBits)
        assertEquals(mapOf(3 to 8), featureBits)
        assertFalse(hasHat)
    }
}
