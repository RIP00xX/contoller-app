package com.virtualcontroller.bt

import org.junit.Assert.assertEquals
import org.junit.Test

class ControllerInputStateTest {
    @Test
    fun outerControlsPreserveInnerStickAndButtons() {
        val state = ControllerInputState()
        state.update { it.copy(leftStickX = 210, buttonsMask = GamepadReport.BUTTON_A) }
        state.update { it.copy(buttonsMask = it.buttonsMask or GamepadReport.BUTTON_L1) }
        val combined = state.update { it.copy(r2Trigger = 180) }
        assertEquals(210, combined.leftStickX)
        assertEquals(GamepadReport.BUTTON_A or GamepadReport.BUTTON_L1, combined.buttonsMask)
        assertEquals(180, combined.r2Trigger)

        val released = state.update { it.copy(buttonsMask = it.buttonsMask and GamepadReport.BUTTON_L1.inv()) }
        assertEquals(GamepadReport.BUTTON_A, released.buttonsMask)
        assertEquals(210, released.leftStickX)
        assertEquals(180, released.r2Trigger)
    }

    @Test
    fun innerControlsPreserveBothOuterTriggers() {
        val state = ControllerInputState()
        state.update { it.copy(l2Trigger = 100, r2Trigger = 220) }
        val report = state.update { it.copy(rightStickY = 40, dPadHat = GamepadReport.DPAD_LEFT) }
        assertEquals(100, report.l2Trigger)
        assertEquals(220, report.r2Trigger)
        assertEquals(40, report.rightStickY)
        assertEquals(GamepadReport.DPAD_LEFT, report.dPadHat)
        assertEquals(GamepadReport(), state.update { GamepadReport() })
    }
}
