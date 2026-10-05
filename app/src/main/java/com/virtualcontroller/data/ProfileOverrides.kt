package com.virtualcontroller.data

import com.virtualcontroller.model.ControllerProfile

/** Saved edits replace a preset with the same ID instead of being shadowed by it. */
fun mergeProfileOverrides(presets: List<ControllerProfile>, saved: List<ControllerProfile>): List<ControllerProfile> =
    (presets + saved).associateBy { it.id }.values.toList()
