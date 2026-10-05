package com.virtualcontroller.model

data class ControllerProfile(
    val id: String,
    val name: String,
    val description: String,
    val isPreset: Boolean = false,
    val elements: List<ControllerElement>
)
