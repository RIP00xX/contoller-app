package com.virtualcontroller.bt

import org.junit.Assert.assertArrayEquals
import org.junit.Test

class TouchpadReportTest {
    @Test
    fun contactCoordinatesUseLittleEndianAndAreBounded() {
        assertArrayEquals(
            byteArrayOf(7, 0, 0x7F, 7, 0, 0, 1),
            TouchpadReport(true, Int.MAX_VALUE, -20).toByteArray()
        )
        assertArrayEquals(
            byteArrayOf(7, 0, 0, 0, 0x37, 4, 1),
            TouchpadReport(true, -1, Int.MAX_VALUE).toByteArray()
        )
    }

    @Test
    fun liftingContactPreservesFinalPositionButReleasesFlagsAndCount() {
        assertArrayEquals(byteArrayOf(0, 0, 44, 1, 100, 0, 0), TouchpadReport(false, 300, 100).toByteArray())
    }
}
