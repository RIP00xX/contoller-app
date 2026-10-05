package com.virtualcontroller.ui.controller

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.virtualcontroller.bt.GamepadReport
import com.virtualcontroller.haptics.HapticFeedbackManager
import com.virtualcontroller.model.ControllerElementType
import com.virtualcontroller.model.ControllerProfile
import com.virtualcontroller.model.TargetScreen
import com.virtualcontroller.ui.components.VirtualButton

@Composable
fun OuterControllerScreen(
    currentProfile: ControllerProfile,
    hapticManager: HapticFeedbackManager,
    onReportStateChanged: (updater: (GamepadReport) -> GamepadReport) -> Unit,
    onViewportChanged: (DpSize) -> Unit = {}
) {
    val density = LocalDensity.current
    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFF0F172A))
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(12.dp)
    ) {
        // These constraints belong to the cover view, not the inner Activity's configuration.
        // Use its shorter edge so rotation cannot turn a shoulder button into a giant control.
        BoxWithConstraints(modifier = Modifier.fillMaxSize().clipToBounds().onSizeChanged {
            if (it.width > 0 && it.height > 0) {
                with(density) { onViewportChanged(DpSize(it.width.toDp(), it.height.toDp())) }
            }
        }) {
            val shortEdge = minOf(maxWidth, maxHeight)
            val outerElements = currentProfile.elements.filter { it.targetScreen == TargetScreen.OUTER_SCREEN }

            outerElements.forEach { elem ->
                val buttonSize = (shortEdge * elem.sizePercent)
                    .coerceIn(64.dp, 144.dp).coerceAtMost(shortEdge)
                val x = (maxWidth * elem.xPercent - buttonSize / 2)
                    .coerceIn(0.dp, (maxWidth - buttonSize).coerceAtLeast(0.dp))
                val y = (maxHeight * elem.yPercent - buttonSize / 2)
                    .coerceIn(0.dp, (maxHeight - buttonSize).coerceAtLeast(0.dp))

                val buttonBit = when (elem.type) {
                    ControllerElementType.BUMPER_L1,
                    ControllerElementType.BUMPER_R1 -> elem.mappedHidBitOrAxis
                    ControllerElementType.TRIGGER_L2,
                    ControllerElementType.TRIGGER_R2 -> 0 // Trigger state is serialized into digital buttons.
                    else -> return@forEach
                }
                key(elem.id, elem.type, elem.mappedHidBitOrAxis) {
                  Box(modifier = Modifier.offset(x, y).size(buttonSize)) {
                    VirtualButton(
                        modifier = Modifier.fillMaxSize(),
                        label = elem.label.removeSuffix(" Bumper").removeSuffix(" Trigger"),
                        buttonBit = buttonBit,
                        opacity = elem.opacity,
                        hapticIntensity = elem.hapticIntensity,
                        onPressedStateChanged = { bit, pressed ->
                            if (pressed) hapticManager.performClickHaptic(elem.hapticIntensity)
                            onReportStateChanged { report ->
                                when (elem.type) {
                                    ControllerElementType.TRIGGER_L2 -> report.copy(
                                        l2Trigger = if (pressed) GamepadReport.MAX_AXIS else GamepadReport.MIN_AXIS
                                    )
                                    ControllerElementType.TRIGGER_R2 -> report.copy(
                                        r2Trigger = if (pressed) GamepadReport.MAX_AXIS else GamepadReport.MIN_AXIS
                                    )
                                    else -> report.copy(buttonsMask = if (pressed) {
                                        report.buttonsMask or bit
                                    } else {
                                        report.buttonsMask and bit.inv()
                                    })
                                }
                            }
                        }
                    )
                  }
                }
            }
        }
    }
}
