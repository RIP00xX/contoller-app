package com.virtualcontroller.data

import com.google.gson.Gson
import com.virtualcontroller.model.ControllerElement
import com.virtualcontroller.model.ControllerElementType
import com.virtualcontroller.model.ControllerProfile
import org.junit.Assert.assertEquals
import org.junit.Test

class ProfileOverridesTest {
    @Test
    fun editedPresetSurvivesSerializationAndReplacesDefault() {
        val preset = ControllerProfile("ps5", "PS5", "", true, listOf(
            ControllerElement("stick", ControllerElementType.JOYSTICK_LEFT, "L3", xPercent = 0.3f, yPercent = 0.7f)
        ))
        val edited = preset.copy(elements = listOf(preset.elements[0].copy(xPercent = 0.6f, sizePercent = 0.35f)))
        val gson = Gson()
        val reloaded = gson.fromJson(gson.toJson(edited), ControllerProfile::class.java)
        val result = mergeProfileOverrides(listOf(preset), listOf(reloaded))
        assertEquals(listOf(edited), result)
    }

    @Test
    fun latestEditWinsWhileOtherPresetsAndCustomProfilesRemain() {
        val preset = ControllerProfile("ps5", "PS5", "", true, emptyList())
        val xbox = preset.copy(id = "xbox")
        val custom = preset.copy(id = "custom", isPreset = false)
        val edited = preset.copy(name = "Edited")
        val newest = preset.copy(name = "Newest")
        assertEquals(listOf(newest, xbox, custom), mergeProfileOverrides(listOf(preset, xbox), listOf(edited, custom, newest)))
        assertEquals(listOf(preset, xbox), mergeProfileOverrides(listOf(preset, xbox), emptyList()))
    }
}
