package com.virtualcontroller.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun VirtualButton(
    modifier: Modifier = Modifier,
    label: String,
    buttonBit: Int,
    accentColor: Color = Color(0xFF00E5FF),
    opacity: Float = 0.85f,
    hapticIntensity: Float = 0.7f,
    onPressedStateChanged: (buttonBit: Int, isPressed: Boolean) -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.90f else 1.0f, label = "buttonScale")

    Box(
        modifier = modifier
            .scale(scale)
            .alpha(opacity)
            .clip(CircleShape)
            .background(
                brush = Brush.radialGradient(
                    colors = if (isPressed) {
                        listOf(accentColor, accentColor.copy(alpha = 0.4f))
                    } else {
                        listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                    }
                )
            )
            .border(
                width = 2.dp,
                color = if (isPressed) accentColor else Color.White.copy(alpha = 0.4f),
                shape = CircleShape
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        onPressedStateChanged(buttonBit, true)
                        tryAwaitRelease()
                        isPressed = false
                        onPressedStateChanged(buttonBit, false)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isPressed) Color.Black else Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }
}
