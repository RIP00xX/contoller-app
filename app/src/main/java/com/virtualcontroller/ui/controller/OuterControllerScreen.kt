package com.virtualcontroller.ui.controller

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.virtualcontroller.bt.GamepadReport
import com.virtualcontroller.haptics.HapticFeedbackManager
import com.virtualcontroller.model.ControllerElementType
import com.virtualcontroller.model.ControllerProfile
import com.virtualcontroller.model.TargetScreen
import com.virtualcontroller.ui.components.VirtualButton
import com.virtualcontroller.ui.components.VirtualTriggerSlider
import kotlin.math.roundToInt

@Composable
fun OuterControllerScreen(
    currentProfile: ControllerProfile,
    hapticManager: HapticFeedbackManager,
    onReportStateChanged: (updater: (GamepadReport) -> GamepadReport) -> Unit
) {
    var canvasSize by remember { mutableStateOf(IntSize(1000, 1000)) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .onSizeChanged { canvasSize = it }
    ) {
        val outerElements = currentProfile.elements.filter { it.targetScreen == TargetScreen.OUTER_SCREEN }

        outerElements.forEach { elem ->
            val widthPx = canvasSize.width * elem.sizePercent
            val heightPx = widthPx

            val posX = (canvasSize.width * elem.xPercent - widthPx / 2f).coerceIn(0f, canvasSize.width - widthPx)
            val posY = (canvasSize.height * elem.yPercent - heightPx / 2f).coerceIn(0f, canvasSize.height - heightPx)

            Box(
                modifier = Modifier
                    .offset { IntOffset(posX.roundToInt(), posY.roundToInt()) }
                    .size(widthPx.dp, heightPx.dp)
            ) {
                when (elem.type) {
                    ControllerElementType.BUMPER_L1,
                    ControllerElementType.BUMPER_R1 -> {
                        VirtualButton(
                            modifier = Modifier.fillMaxSize(),
                            label = elem.label,
                            buttonBit = elem.mappedHidBitOrAxis,
                            opacity = elem.opacity,
                            hapticIntensity = elem.hapticIntensity,
                            onPressedStateChanged = { bit, pressed ->
                                if (pressed) hapticManager.performClickHaptic(elem.hapticIntensity)
                                onReportStateChanged { rep ->
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
                            accentColor = Color(0xFFFF5252),
                            onPressureChanged = { l2Val ->
                                hapticManager.performTriggerHaptic(l2Val / 255f, elem.hapticIntensity)
                                onReportStateChanged { rep -> rep.copy(l2Trigger = l2Val) }
                            }
                        )
                    }
                    ControllerElementType.TRIGGER_R2 -> {
                        VirtualTriggerSlider(
                            modifier = Modifier.fillMaxSize(),
                            label = elem.label,
                            opacity = elem.opacity,
                            accentColor = Color(0xFF4CAF50),
                            onPressureChanged = { r2Val ->
                                hapticManager.performTriggerHaptic(r2Val / 255f, elem.hapticIntensity)
                                onReportStateChanged { rep -> rep.copy(r2Trigger = r2Val) }
                            }
                        )
                    }
                    else -> {}
                }
            }
        }
    }
}
