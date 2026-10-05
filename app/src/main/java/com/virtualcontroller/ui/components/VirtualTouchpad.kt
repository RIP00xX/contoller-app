package com.virtualcontroller.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.virtualcontroller.bt.GamepadReport
import com.virtualcontroller.bt.TouchpadReport
import kotlin.math.roundToInt

@Composable
fun VirtualTouchpad(
    modifier: Modifier = Modifier,
    onTouchChanged: (TouchpadReport) -> Unit,
    onClickChanged: (Boolean) -> Unit
) {
    val sendTouch by rememberUpdatedState(onTouchChanged)
    var touching by remember { mutableStateOf(false) }
    Column(modifier = modifier.clip(RoundedCornerShape(12.dp))) {
        Box(
            modifier = Modifier.fillMaxWidth().weight(1f)
                .background(Color(0xFF1E293B))
                .border(1.dp, if (touching) Color(0xFF00E5FF) else Color(0xFF475569), RoundedCornerShape(12.dp))
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val down = awaitFirstDown()
                        down.consume()
                        var report = TouchpadReport(
                            true,
                            (down.position.x / size.width.coerceAtLeast(1) * TouchpadReport.MAX_X).roundToInt(),
                            (down.position.y / size.height.coerceAtLeast(1) * TouchpadReport.MAX_Y).roundToInt()
                        )
                        touching = true
                        try {
                            sendTouch(report)
                            while (true) {
                                val change = awaitPointerEvent().changes.firstOrNull { it.id == down.id } ?: break
                                change.consume()
                                if (!change.pressed) break
                                report = TouchpadReport(
                                    true,
                                    (change.position.x / size.width.coerceAtLeast(1) * TouchpadReport.MAX_X).roundToInt(),
                                    (change.position.y / size.height.coerceAtLeast(1) * TouchpadReport.MAX_Y).roundToInt()
                                )
                                sendTouch(report)
                            }
                        } finally {
                            touching = false
                            sendTouch(report.copy(touching = false))
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Text("Touchpad", color = Color(0xFF94A3B8), fontSize = 12.sp)
        }
        VirtualButton(
            modifier = Modifier.fillMaxWidth().height(36.dp),
            label = "Click",
            buttonBit = GamepadReport.BUTTON_TOUCHPAD,
            shape = RoundedCornerShape(6.dp),
            onPressedStateChanged = { _, pressed -> onClickChanged(pressed) }
        )
    }
}
