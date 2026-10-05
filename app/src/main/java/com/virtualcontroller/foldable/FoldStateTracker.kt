package com.virtualcontroller.foldable

import android.app.Activity
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoTracker
import androidx.window.layout.WindowLayoutInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class DevicePosture {
    CLOSED,       // Standard single screen mode
    HALF_OPENED,  // Flex Mode (laptop angle)
    FLAT          // Fully unfolded dual-display gamepad mode
}

class FoldStateTracker(private val activity: Activity) {

    val postureFlow: Flow<DevicePosture> = WindowInfoTracker.getOrCreate(activity)
        .windowLayoutInfo(activity)
        .map { layoutInfo: WindowLayoutInfo ->
            val foldingFeature = layoutInfo.displayFeatures
                .filterIsInstance<FoldingFeature>()
                .firstOrNull()

            if (foldingFeature == null) {
                DevicePosture.CLOSED
            } else {
                when (foldingFeature.state) {
                    FoldingFeature.State.FLAT -> DevicePosture.FLAT
                    FoldingFeature.State.HALF_OPENED -> DevicePosture.HALF_OPENED
                    else -> DevicePosture.CLOSED
                }
            }
        }
}
