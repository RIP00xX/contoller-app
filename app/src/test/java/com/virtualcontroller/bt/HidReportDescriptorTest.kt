package com.virtualcontroller.bt

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HidReportDescriptorTest {
    @Test
    fun reportSizesMatchWirePayloadsAndHatSupportsNeutral() {
        val descriptor = HidReportDescriptor.CONTROLLER_DESCRIPTOR
        var offset = 0
        var reportId = 0
        var size = 0
        var count = 0
        var usagePage = 0
        var lastUsage = 0
        var hatHasNullState = false
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
                        if (usagePage == 1 && lastUsage == 0x39) hatHasNullState = value and 0x40 != 0
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
        assertTrue(hatHasNullState)
    }
}
