package com.virtualcontroller.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput

import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.virtualcontroller.model.ControllerElement
import com.virtualcontroller.model.ControllerElementType
import com.virtualcontroller.model.ControllerProfile
import com.virtualcontroller.model.TargetScreen
import kotlin.math.roundToInt



@Composable
fun LayoutEditorScreen(
    profile: ControllerProfile,
    outerViewport: DpSize,
    onSaveProfile: (updatedProfile: ControllerProfile) -> Unit,
    onCancel: () -> Unit
) {
    var elements by remember { mutableStateOf(profile.elements) }
    var selectedElementId by remember { mutableStateOf<String?>(null) }
    var canvasSize by remember { mutableStateOf(IntSize(1000, 1000)) }
    var editingScreen by remember { mutableStateOf(TargetScreen.INNER_SCREEN) }

    val density = LocalDensity.current
    val selectedElement = elements.find { it.id == selectedElementId }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
    ) {
        // --- Drag & Drop Canvas Overlay ---
        Box(modifier = Modifier.fillMaxSize().padding(top = 80.dp, end = 280.dp)) {
          val previewModifier = if (editingScreen == TargetScreen.OUTER_SCREEN) {
              Modifier.align(Alignment.Center).aspectRatio(outerViewport.width / outerViewport.height)
          } else Modifier.fillMaxSize()
          Box(modifier = previewModifier.background(Color(0xFF090D16)).onSizeChanged { canvasSize = it }) {
            elements.filter { it.targetScreen == editingScreen }.forEach { elem ->
                val isSelected = elem.id == selectedElementId
                val elemWidthPx = if (editingScreen == TargetScreen.OUTER_SCREEN) {
                    val actualSize = (minOf(outerViewport.width, outerViewport.height) * elem.sizePercent)
                        .coerceIn(64.dp, 144.dp).coerceAtMost(minOf(outerViewport.width, outerViewport.height))
                    canvasSize.width * (actualSize / outerViewport.width)
                } else canvasSize.width * elem.sizePercent
                val elemHeightPx = if (elem.type == ControllerElementType.TOUCHPAD) elemWidthPx * 0.65f else elemWidthPx

                val elemWidthDp = with(density) { elemWidthPx.toDp() }
                val elemHeightDp = with(density) { elemHeightPx.toDp() }

                val posX = (canvasSize.width * elem.xPercent - elemWidthPx / 2f)
                    .coerceIn(0f, (canvasSize.width - elemWidthPx).coerceAtLeast(0f))
                val posY = (canvasSize.height * elem.yPercent - elemHeightPx / 2f)
                    .coerceIn(0f, (canvasSize.height - elemHeightPx).coerceAtLeast(0f))

                Box(
                    modifier = Modifier
                        .offset { IntOffset(posX.roundToInt(), posY.roundToInt()) }
                        .size(elemWidthDp, elemHeightDp)

                        .border(
                            width = if (isSelected) 3.dp else 1.dp,
                            color = if (isSelected) Color(0xFF00E5FF) else Color.White.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .background(
                            color = if (elem.targetScreen == TargetScreen.OUTER_SCREEN) {
                                Color(0xFFE91E63).copy(alpha = 0.3f)
                            } else {
                                Color(0xFF009688).copy(alpha = 0.3f)
                            },
                            shape = RoundedCornerShape(12.dp)
                        )
                        .pointerInput(elem.id) {
                            detectDragGestures(
                                onDragStart = { selectedElementId = elem.id },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    val current = elements.find { it.id == elem.id } ?: return@detectDragGestures
                                    val newXPercent = (current.xPercent + dragAmount.x / canvasSize.width.coerceAtLeast(1)).coerceIn(0.05f, 0.95f)
                                    val newYPercent = (current.yPercent + dragAmount.y / canvasSize.height.coerceAtLeast(1)).coerceIn(0.05f, 0.95f)

                                    elements = elements.map {
                                        if (it.id == elem.id) it.copy(xPercent = newXPercent, yPercent = newYPercent) else it
                                    }
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = elem.label,
                            color = Color.White,
                            fontSize = 12.sp
                        )
                        Text(
                            text = if (elem.targetScreen == TargetScreen.OUTER_SCREEN) "[OUTER]" else "[INNER]",
                            color = if (elem.targetScreen == TargetScreen.OUTER_SCREEN) Color(0xFFFF4081) else Color(0xFF64FFDA),
                            fontSize = 10.sp
                        )
                    }
                }
            }
          }
        }

        // --- Top Bar Actions ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = {
                editingScreen = if (editingScreen == TargetScreen.INNER_SCREEN) TargetScreen.OUTER_SCREEN else TargetScreen.INNER_SCREEN
                selectedElementId = null
            }) {
                Text(if (editingScreen == TargetScreen.INNER_SCREEN) "Editing inner · Switch" else "Editing outer · Switch")
            }

            Row {
                Button(
                    onClick = onCancel,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Cancel")
                    Spacer(Modifier.width(4.dp))
                    Text("Cancel")
                }
                Spacer(Modifier.width(12.dp))
                Button(
                    onClick = { onSaveProfile(profile.copy(elements = elements)) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF))
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Save", tint = Color.Black)
                    Spacer(Modifier.width(4.dp))
                    Text("Save Layout", color = Color.Black)
                }
            }
        }

        // --- Side Property Inspector Panel ---
        selectedElement?.let { elem ->
            Card(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .width(280.dp)
                    .fillMaxHeight(0.85f)
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.95f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Element Inspector",
                        color = Color(0xFF00E5FF),
                        fontSize = 16.sp,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(text = "Name: ${elem.label}", color = Color.White, fontSize = 14.sp)
                    Spacer(Modifier.height(16.dp))

                    // Size Slider
                    Text(text = "Size Scale: ${(elem.sizePercent * 100).toInt()}%", color = Color.Gray, fontSize = 12.sp)
                    Slider(
                        value = elem.sizePercent,
                        onValueChange = { newSize ->
                            elements = elements.map { if (it.id == elem.id) it.copy(sizePercent = newSize) else it }
                        },
                        valueRange = 0.08f..0.50f,
                        colors = SliderDefaults.colors(thumbColor = Color(0xFF00E5FF), activeTrackColor = Color(0xFF00E5FF))
                    )

                    // Opacity Slider
                    Text(text = "Opacity: ${(elem.opacity * 100).toInt()}%", color = Color.Gray, fontSize = 12.sp)
                    Slider(
                        value = elem.opacity,
                        onValueChange = { newOp ->
                            elements = elements.map { if (it.id == elem.id) it.copy(opacity = newOp) else it }
                        },
                        valueRange = 0.1f..1.0f,
                        colors = SliderDefaults.colors(thumbColor = Color(0xFF00E5FF), activeTrackColor = Color(0xFF00E5FF))
                    )

                    // Haptic Slider
                    Text(text = "Haptic Vibration: ${(elem.hapticIntensity * 100).toInt()}%", color = Color.Gray, fontSize = 12.sp)
                    Slider(
                        value = elem.hapticIntensity,
                        onValueChange = { newHap ->
                            elements = elements.map { if (it.id == elem.id) it.copy(hapticIntensity = newHap) else it }
                        },
                        valueRange = 0.0f..1.0f,
                        colors = SliderDefaults.colors(thumbColor = Color(0xFF00E5FF), activeTrackColor = Color(0xFF00E5FF))
                    )

                    Spacer(Modifier.height(12.dp))

                    // Cross-Screen Target Toggle (Inner vs Outer Cover Screen)
                    Button(
                        enabled = elem.type != ControllerElementType.TOUCHPAD,
                        onClick = {
                            val nextTarget = if (elem.targetScreen == TargetScreen.INNER_SCREEN) TargetScreen.OUTER_SCREEN else TargetScreen.INNER_SCREEN
                            elements = elements.map { if (it.id == elem.id) it.copy(targetScreen = nextTarget) else it }
                            editingScreen = nextTarget
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (elem.targetScreen == TargetScreen.OUTER_SCREEN) Color(0xFFE91E63) else Color(0xFF009688)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Screen Target")
                        Spacer(Modifier.width(8.dp))
                        Text(text = if (elem.targetScreen == TargetScreen.OUTER_SCREEN) "Target: Outer Screen" else "Target: Inner Screen")
                    }

                    Spacer(Modifier.weight(1.0f))

                    IconButton(
                        onClick = {
                            elements = elements.filterNot { it.id == elem.id }
                            selectedElementId = null
                        },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Delete", tint = Color.Red)

                    }
                }
            }
        }
    }
}
