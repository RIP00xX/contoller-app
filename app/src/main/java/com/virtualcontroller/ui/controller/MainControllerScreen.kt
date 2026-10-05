package com.virtualcontroller.ui.controller

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings

import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.virtualcontroller.bt.GamepadReport
import com.virtualcontroller.bt.HidConnectionState
import com.virtualcontroller.foldable.DevicePosture
import com.virtualcontroller.foldable.OuterScreenStatus
import com.virtualcontroller.haptics.HapticFeedbackManager
import com.virtualcontroller.model.ControllerElementType
import com.virtualcontroller.model.ControllerProfile
import com.virtualcontroller.model.TargetScreen
import com.virtualcontroller.ui.components.VirtualButton
import com.virtualcontroller.ui.components.VirtualDPad
import com.virtualcontroller.ui.components.VirtualJoystick
import com.virtualcontroller.ui.components.VirtualSteeringWheel
import com.virtualcontroller.ui.components.VirtualTriggerSlider
import kotlin.math.roundToInt



@Composable
fun MainControllerScreen(
    currentProfile: ControllerProfile,
    allProfiles: List<ControllerProfile>,
    connectionState: HidConnectionState,
    devicePosture: DevicePosture,
    outerScreenStatus: OuterScreenStatus,
    onEnableOuterScreen: () -> Unit,
    hapticManager: HapticFeedbackManager,
    onSelectProfile: (profile: ControllerProfile) -> Unit,
    onOpenEditor: () -> Unit,
    onReportStateChanged: (updater: (GamepadReport) -> GamepadReport) -> Unit
) {
    var canvasSize by remember { mutableStateOf(IntSize(1000, 1000)) }
    var profileMenuExpanded by remember { mutableStateOf(false) }

    val density = LocalDensity.current

    fun updateAndSendReport(updater: (GamepadReport) -> GamepadReport) {
        onReportStateChanged(updater)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF090D16))
            .onSizeChanged { canvasSize = it }
    ) {
        // --- Inner Screen Controls Canvas ---
        val innerElements = currentProfile.elements.filter { it.targetScreen == TargetScreen.INNER_SCREEN }

        innerElements.forEach { elem ->
            val elemWidthPx = canvasSize.width * elem.sizePercent
            val elemHeightPx = elemWidthPx

            val elemWidthDp = with(density) { elemWidthPx.toDp() }
            val elemHeightDp = elemWidthDp

            val posX = (canvasSize.width * elem.xPercent - elemWidthPx / 2f)
                .coerceIn(0f, (canvasSize.width - elemWidthPx).coerceAtLeast(0f))
            val posY = (canvasSize.height * elem.yPercent - elemHeightPx / 2f)
                .coerceIn(0f, (canvasSize.height - elemHeightPx).coerceAtLeast(0f))

            Box(
                modifier = Modifier
                    .offset { IntOffset(posX.roundToInt(), posY.roundToInt()) }
                    .size(elemWidthDp, elemHeightDp)
            ) {

                when (elem.type) {
                    ControllerElementType.JOYSTICK_LEFT -> {
                        VirtualJoystick(
                            modifier = Modifier.fillMaxSize(),
                            label = elem.label,
                            opacity = elem.opacity,
                            onPositionChanged = { lx, ly, isL3 ->
                                updateAndSendReport { rep ->
                                    val newButtons = if (isL3) (rep.buttonsMask or GamepadReport.BUTTON_L3) else (rep.buttonsMask and GamepadReport.BUTTON_L3.inv())
                                    rep.copy(leftStickX = lx, leftStickY = ly, buttonsMask = newButtons)
                                }
                            }
                        )
                    }
                    ControllerElementType.JOYSTICK_RIGHT -> {
                        VirtualJoystick(
                            modifier = Modifier.fillMaxSize(),
                            label = elem.label,
                            opacity = elem.opacity,
                            onPositionChanged = { rx, ry, isR3 ->
                                updateAndSendReport { rep ->
                                    val newButtons = if (isR3) (rep.buttonsMask or GamepadReport.BUTTON_R3) else (rep.buttonsMask and GamepadReport.BUTTON_R3.inv())
                                    rep.copy(rightStickX = rx, rightStickY = ry, buttonsMask = newButtons)
                                }
                            }
                        )
                    }
                    ControllerElementType.DPAD -> {
                        VirtualDPad(
                            modifier = Modifier.fillMaxSize(),
                            opacity = elem.opacity,
                            onDirectionChanged = { hat ->
                                hapticManager.performTickHaptic(elem.hapticIntensity)
                                updateAndSendReport { rep -> rep.copy(dPadHat = hat) }
                            }
                        )
                    }
                    ControllerElementType.BUTTON_A,
                    ControllerElementType.BUTTON_B,
                    ControllerElementType.BUTTON_X,
                    ControllerElementType.BUTTON_Y,
                    ControllerElementType.BUMPER_L1,
                    ControllerElementType.BUMPER_R1,
                    ControllerElementType.BUTTON_START,
                    ControllerElementType.BUTTON_SELECT,
                    ControllerElementType.BUTTON_MODE -> {
                        VirtualButton(
                            modifier = Modifier.fillMaxSize(),
                            label = elem.label,
                            buttonBit = elem.mappedHidBitOrAxis,
                            opacity = elem.opacity,
                            hapticIntensity = elem.hapticIntensity,
                            onPressedStateChanged = { bit, pressed ->
                                if (pressed) hapticManager.performClickHaptic(elem.hapticIntensity)
                                updateAndSendReport { rep ->
                                    val newMask = if (pressed) (rep.buttonsMask or bit) else (rep.buttonsMask and bit.inv())
                                    rep.copy(buttonsMask = newMask)
                                }
                            }
                        )
                    }
                    ControllerElementType.TRIGGER_L2 -> {
                        VirtualTriggerSlider(
                            modifier = Modifier.fillMaxSize(),
                            label = elem.label,
                            opacity = elem.opacity,
                            onPressureChanged = { l2Val ->
                                hapticManager.performTriggerHaptic(l2Val / 255f, elem.hapticIntensity)
                                updateAndSendReport { rep -> rep.copy(l2Trigger = l2Val) }
                            }
                        )
                    }
                    ControllerElementType.TRIGGER_R2 -> {
                        VirtualTriggerSlider(
                            modifier = Modifier.fillMaxSize(),
                            label = elem.label,
                            opacity = elem.opacity,
                            onPressureChanged = { r2Val ->
                                hapticManager.performTriggerHaptic(r2Val / 255f, elem.hapticIntensity)
                                updateAndSendReport { rep -> rep.copy(r2Trigger = r2Val) }
                            }
                        )
                    }
                    ControllerElementType.STEERING_WHEEL -> {
                        VirtualSteeringWheel(
                            modifier = Modifier.fillMaxSize(),
                            opacity = elem.opacity,
                            onSteeringAngleChanged = { hidX ->
                                updateAndSendReport { rep -> rep.copy(leftStickX = hidX) }
                            }
                        )
                    }
                }
            }
        }

        // --- Header Status Bar ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Bluetooth Connection Status Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF1E293B).copy(alpha = 0.85f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                val (statusText, statusColor) = when (connectionState) {
                    is HidConnectionState.Connected -> Pair("Connected: ${connectionState.deviceName}", Color(0xFF00E5FF))
                    is HidConnectionState.Registered -> Pair("Ready to Pair", Color(0xFFFFB74D))
                    is HidConnectionState.Initializing -> Pair("Initializing BT...", Color.Yellow)
                    is HidConnectionState.Error -> Pair(connectionState.message, Color.Red)
                    else -> Pair("Bluetooth Idle", Color.Gray)
                }

                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(statusColor)
                )
                Spacer(Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.Settings,

                    contentDescription = "Bluetooth Status",
                    tint = statusColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(text = statusText, color = Color.White, fontSize = 12.sp)
            }

            // Posture Badge
            if (devicePosture != DevicePosture.CLOSED) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFFE91E63).copy(alpha = 0.85f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (devicePosture == DevicePosture.FLAT) "UNFOLDED" else "FLEX MODE",
                        color = Color.White,
                        fontSize = 11.sp
                    )
                }
            }

            // Profile Dropdown & Edit Mode Actions
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box {
                    Button(
                        onClick = { profileMenuExpanded = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Switch Profile", tint = Color(0xFF00E5FF))
                        Spacer(Modifier.width(6.dp))
                        Text(text = currentProfile.name, color = Color.White, fontSize = 12.sp)
                    }

                    DropdownMenu(
                        expanded = profileMenuExpanded,
                        onDismissRequest = { profileMenuExpanded = false }
                    ) {
                        allProfiles.forEach { prof ->
                            DropdownMenuItem(
                                text = { Text(prof.name + if (prof.isPreset) " [Preset]" else "") },
                                onClick = {
                                    onSelectProfile(prof)
                                    profileMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.width(8.dp))

                Button(
                    onClick = onOpenEditor,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Layout", tint = Color.Black)
                    Spacer(Modifier.width(4.dp))
                    Text(text = "Edit Mode", color = Color.Black, fontSize = 12.sp)
                }
            }
        }

        Column(
            modifier = Modifier.align(Alignment.BottomCenter)
                .background(Color(0xFF1E293B).copy(alpha = 0.9f), RoundedCornerShape(12.dp))
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(outerScreenStatus.message, color = Color.White, fontSize = 12.sp)
            if (outerScreenStatus.canStart && currentProfile.elements.any { it.targetScreen == TargetScreen.OUTER_SCREEN }) {
                Button(onClick = onEnableOuterScreen) {
                    Text("Enable outer controls")
                }
            }
        }
    }
}
