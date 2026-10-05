package com.virtualcontroller.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.virtualcontroller.bt.GamepadReport
import com.virtualcontroller.model.ControllerElement
import com.virtualcontroller.model.ControllerElementType
import com.virtualcontroller.model.ControllerProfile
import com.virtualcontroller.model.TargetScreen
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "controller_profiles")

class ProfileRepository(private val context: Context) {

    private val gson = Gson()
    private val PROFILES_KEY = stringPreferencesKey("custom_profiles_json")
    private val ACTIVE_PROFILE_ID_KEY = stringPreferencesKey("active_profile_id")

    val defaultProfiles: List<ControllerProfile> = listOf(
        createPlayStationProfile(),
        createXboxProfile(),
        createRacingProfile()
    )

    fun getActiveProfileId(): Flow<String> {
        return context.dataStore.data.map { prefs ->
            prefs[ACTIVE_PROFILE_ID_KEY] ?: defaultProfiles.first().id
        }
    }

    suspend fun setActiveProfileId(profileId: String) {
        context.dataStore.edit { prefs ->
            prefs[ACTIVE_PROFILE_ID_KEY] = profileId
        }
    }

    fun getSavedProfiles(): Flow<List<ControllerProfile>> {
        return context.dataStore.data.map { prefs ->
            val json = prefs[PROFILES_KEY]
            if (json.isNullOrEmpty()) {
                defaultProfiles
            } else {
                val type = object : TypeToken<List<ControllerProfile>>() {}.type
                val customList: List<ControllerProfile> = gson.fromJson(json, type) ?: emptyList()
                defaultProfiles + customList
            }
        }
    }

    suspend fun saveCustomProfile(profile: ControllerProfile) {
        context.dataStore.edit { prefs ->
            val currentJson = prefs[PROFILES_KEY]
            val type = object : TypeToken<List<ControllerProfile>>() {}.type
            val existing: MutableList<ControllerProfile> = if (currentJson.isNullOrEmpty()) {
                mutableListOf()
            } else {
                (gson.fromJson(currentJson, type) as? List<ControllerProfile>)?.toMutableList() ?: mutableListOf()
            }

            val index = existing.indexOfFirst { it.id == profile.id }
            if (index >= 0) {
                existing[index] = profile
            } else {
                existing.add(profile)
            }

            prefs[PROFILES_KEY] = gson.toJson(existing)
        }
    }

    suspend fun deleteCustomProfile(profileId: String) {
        context.dataStore.edit { prefs ->
            val currentJson = prefs[PROFILES_KEY] ?: return@edit
            val type = object : TypeToken<List<ControllerProfile>>() {}.type
            val existing: MutableList<ControllerProfile> = (gson.fromJson(currentJson, type) as? List<ControllerProfile>)?.toMutableList() ?: mutableListOf()
            existing.removeAll { it.id == profileId }
            prefs[PROFILES_KEY] = gson.toJson(existing)
        }
    }


    // --- PRESET GENERATION LOGIC ---

    private fun createPlayStationProfile(): ControllerProfile {
        return ControllerProfile(
            id = "preset_ps5",
            name = "DualSense PS5 Layout",
            description = "Symmetrical joysticks with rear outer-screen triggers",
            isPreset = true,
            elements = listOf(
                // Inner Screen Controls
                ControllerElement("dpad", ControllerElementType.DPAD, "D-Pad", TargetScreen.INNER_SCREEN, 0.18f, 0.35f, 0.28f, 0.85f, 0.7f),
                ControllerElement("joy_left", ControllerElementType.JOYSTICK_LEFT, "L3 Stick", TargetScreen.INNER_SCREEN, 0.30f, 0.72f, 0.26f, 0.85f, 0.8f, GamepadReport.BUTTON_L3),
                ControllerElement("joy_right", ControllerElementType.JOYSTICK_RIGHT, "R3 Stick", TargetScreen.INNER_SCREEN, 0.70f, 0.72f, 0.26f, 0.85f, 0.8f, GamepadReport.BUTTON_R3),
                ControllerElement("btn_sq", ControllerElementType.BUTTON_X, "Square", TargetScreen.INNER_SCREEN, 0.72f, 0.35f, 0.11f, 0.90f, 0.7f, GamepadReport.BUTTON_X),
                ControllerElement("btn_tri", ControllerElementType.BUTTON_Y, "Triangle", TargetScreen.INNER_SCREEN, 0.82f, 0.22f, 0.11f, 0.90f, 0.7f, GamepadReport.BUTTON_Y),
                ControllerElement("btn_cross", ControllerElementType.BUTTON_A, "Cross", TargetScreen.INNER_SCREEN, 0.82f, 0.48f, 0.11f, 0.90f, 0.7f, GamepadReport.BUTTON_A),
                ControllerElement("btn_circle", ControllerElementType.BUTTON_B, "Circle", TargetScreen.INNER_SCREEN, 0.92f, 0.35f, 0.11f, 0.90f, 0.7f, GamepadReport.BUTTON_B),
                ControllerElement("btn_share", ControllerElementType.BUTTON_SELECT, "Create", TargetScreen.INNER_SCREEN, 0.40f, 0.15f, 0.08f, 0.80f, 0.5f, GamepadReport.BUTTON_SELECT),
                ControllerElement("btn_options", ControllerElementType.BUTTON_START, "Options", TargetScreen.INNER_SCREEN, 0.60f, 0.15f, 0.08f, 0.80f, 0.5f, GamepadReport.BUTTON_START),
                ControllerElement("btn_ps", ControllerElementType.BUTTON_MODE, "PS", TargetScreen.INNER_SCREEN, 0.50f, 0.25f, 0.09f, 0.80f, 0.6f, GamepadReport.BUTTON_MODE),

                // Outer Cover Screen Triggers (Foldable Rear Grip)
                ControllerElement("outer_l1", ControllerElementType.BUMPER_L1, "L1 Bumper", TargetScreen.OUTER_SCREEN, 0.25f, 0.20f, 0.35f, 0.90f, 0.8f, GamepadReport.BUTTON_L1),
                ControllerElement("outer_r1", ControllerElementType.BUMPER_R1, "R1 Bumper", TargetScreen.OUTER_SCREEN, 0.75f, 0.20f, 0.35f, 0.90f, 0.8f, GamepadReport.BUTTON_R1),
                ControllerElement("outer_l2", ControllerElementType.TRIGGER_L2, "L2 Trigger", TargetScreen.OUTER_SCREEN, 0.25f, 0.65f, 0.40f, 0.90f, 0.9f),
                ControllerElement("outer_r2", ControllerElementType.TRIGGER_R2, "R2 Trigger", TargetScreen.OUTER_SCREEN, 0.75f, 0.65f, 0.40f, 0.90f, 0.9f)
            )
        )
    }

    private fun createXboxProfile(): ControllerProfile {
        return ControllerProfile(
            id = "preset_xbox",
            name = "Xbox Wireless Layout",
            description = "Asymmetrical joysticks with high-sensitivity triggers",
            isPreset = true,
            elements = listOf(
                // Inner Screen Controls
                ControllerElement("joy_left", ControllerElementType.JOYSTICK_LEFT, "LS", TargetScreen.INNER_SCREEN, 0.18f, 0.32f, 0.28f, 0.85f, 0.8f, GamepadReport.BUTTON_L3),
                ControllerElement("dpad", ControllerElementType.DPAD, "D-Pad", TargetScreen.INNER_SCREEN, 0.32f, 0.72f, 0.26f, 0.85f, 0.7f),
                ControllerElement("joy_right", ControllerElementType.JOYSTICK_RIGHT, "RS", TargetScreen.INNER_SCREEN, 0.68f, 0.72f, 0.26f, 0.85f, 0.8f, GamepadReport.BUTTON_R3),
                ControllerElement("btn_x", ControllerElementType.BUTTON_X, "X", TargetScreen.INNER_SCREEN, 0.72f, 0.35f, 0.11f, 0.90f, 0.7f, GamepadReport.BUTTON_X),
                ControllerElement("btn_y", ControllerElementType.BUTTON_Y, "Y", TargetScreen.INNER_SCREEN, 0.82f, 0.22f, 0.11f, 0.90f, 0.7f, GamepadReport.BUTTON_Y),
                ControllerElement("btn_a", ControllerElementType.BUTTON_A, "A", TargetScreen.INNER_SCREEN, 0.82f, 0.48f, 0.11f, 0.90f, 0.7f, GamepadReport.BUTTON_A),
                ControllerElement("btn_b", ControllerElementType.BUTTON_B, "B", TargetScreen.INNER_SCREEN, 0.92f, 0.35f, 0.11f, 0.90f, 0.7f, GamepadReport.BUTTON_B),
                ControllerElement("btn_view", ControllerElementType.BUTTON_SELECT, "View", TargetScreen.INNER_SCREEN, 0.40f, 0.15f, 0.08f, 0.80f, 0.5f, GamepadReport.BUTTON_SELECT),
                ControllerElement("btn_menu", ControllerElementType.BUTTON_START, "Menu", TargetScreen.INNER_SCREEN, 0.60f, 0.15f, 0.08f, 0.80f, 0.5f, GamepadReport.BUTTON_START),
                ControllerElement("btn_xbox", ControllerElementType.BUTTON_MODE, "Xbox", TargetScreen.INNER_SCREEN, 0.50f, 0.25f, 0.09f, 0.80f, 0.6f, GamepadReport.BUTTON_MODE),

                // Outer Screen Rear Triggers
                ControllerElement("outer_l1", ControllerElementType.BUMPER_L1, "LB", TargetScreen.OUTER_SCREEN, 0.25f, 0.20f, 0.35f, 0.90f, 0.8f, GamepadReport.BUTTON_L1),
                ControllerElement("outer_r1", ControllerElementType.BUMPER_R1, "RB", TargetScreen.OUTER_SCREEN, 0.75f, 0.20f, 0.35f, 0.90f, 0.8f, GamepadReport.BUTTON_R1),
                ControllerElement("outer_l2", ControllerElementType.TRIGGER_L2, "LT", TargetScreen.OUTER_SCREEN, 0.25f, 0.65f, 0.40f, 0.90f, 0.9f),
                ControllerElement("outer_r2", ControllerElementType.TRIGGER_R2, "RT", TargetScreen.OUTER_SCREEN, 0.75f, 0.65f, 0.40f, 0.90f, 0.9f)
            )
        )
    }

    private fun createRacingProfile(): ControllerProfile {
        return ControllerProfile(
            id = "preset_racing",
            name = "Racing Wheel & Throttle",
            description = "Central steering wheel with outer-screen analog brake and throttle sliders",
            isPreset = true,
            elements = listOf(
                // Inner Screen Steering Wheel & Quick Actions
                ControllerElement("wheel", ControllerElementType.STEERING_WHEEL, "Steering", TargetScreen.INNER_SCREEN, 0.50f, 0.55f, 0.55f, 0.90f, 0.8f),
                ControllerElement("btn_nos", ControllerElementType.BUTTON_A, "NOS Boost", TargetScreen.INNER_SCREEN, 0.85f, 0.30f, 0.15f, 0.90f, 0.9f, GamepadReport.BUTTON_A),
                ControllerElement("btn_handbrake", ControllerElementType.BUTTON_X, "Handbrake", TargetScreen.INNER_SCREEN, 0.15f, 0.30f, 0.15f, 0.90f, 0.9f, GamepadReport.BUTTON_X),
                ControllerElement("btn_pause", ControllerElementType.BUTTON_START, "Pause", TargetScreen.INNER_SCREEN, 0.50f, 0.15f, 0.08f, 0.80f, 0.5f, GamepadReport.BUTTON_START),

                // Outer Screen Throttle & Brake Analog Sliders
                ControllerElement("outer_l2", ControllerElementType.TRIGGER_L2, "Brake (L2)", TargetScreen.OUTER_SCREEN, 0.25f, 0.50f, 0.45f, 0.95f, 0.9f),
                ControllerElement("outer_r2", ControllerElementType.TRIGGER_R2, "Throttle (R2)", TargetScreen.OUTER_SCREEN, 0.75f, 0.50f, 0.45f, 0.95f, 0.9f)
            )
        )
    }
}
