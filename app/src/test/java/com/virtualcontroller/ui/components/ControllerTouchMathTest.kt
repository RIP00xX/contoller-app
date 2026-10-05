package com.virtualcontroller.ui.components

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class ControllerTouchMathTest {
    @Test
    fun dpadTapsSelectAllEightSectorsAndCenterReleases() {
        val positions = listOf(50f to 0f, 100f to 0f, 100f to 50f, 100f to 100f,
            50f to 100f, 0f to 100f, 0f to 50f, 0f to 0f)
        positions.forEachIndexed { direction, (x, y) ->
            assertEquals(direction, ControllerTouchMath.dPad(x, y, 100f, 100f))
        }
        assertEquals(8, ControllerTouchMath.dPad(50f, 50f, 100f, 100f))
    }

    @Test
    fun stickUsesTouchPositionAndClampsOutsideItsCircle() {
        val center = ControllerTouchMath.stick(100f, 100f, 200f, 200f)
        assertEquals(128, center.x)
        assertEquals(128, center.y)
        assertFalse(center.edgeClick)
        assertEquals(0, ControllerTouchMath.stick(100f, -100f, 200f, 200f).y)
        assertEquals(255, ControllerTouchMath.stick(100f, 300f, 200f, 200f).y)
        assertEquals(0, ControllerTouchMath.stick(-100f, 100f, 200f, 200f).x)
        assertEquals(255, ControllerTouchMath.stick(300f, 100f, 200f, 200f).x)
    }
}
