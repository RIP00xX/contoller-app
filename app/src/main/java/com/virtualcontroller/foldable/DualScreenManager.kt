package com.virtualcontroller.foldable

import android.content.Context
import android.hardware.display.DisplayManager
import android.os.Build
import android.util.Log
import android.view.Display
import androidx.compose.runtime.Composable

class DualScreenManager(private val context: Context) {

    private val TAG = "DualScreenManager"
    private val displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
    private var presentationWindow: OuterScreenPresentation? = null

    /**
     * Finds secondary display (Cover display when unfolded) and presents UI content.
     */
    fun showOuterScreenContent(content: @Composable () -> Unit) {
        dismissOuterScreen()

        val displays = displayManager.displays
        Log.d(TAG, "Available displays count: ${displays.size}")

        // Secondary display for outer screen presentation
        val outerDisplay: Display? = displays.firstOrNull { it.displayId != Display.DEFAULT_DISPLAY }

        if (outerDisplay != null) {
            try {
                presentationWindow = OuterScreenPresentation(context, outerDisplay, content).apply {
                    show()
                }
                Log.d(TAG, "Successfully showed OuterScreenPresentation on displayId=${outerDisplay.displayId}")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to launch OuterScreenPresentation", e)
            }
        } else {
            Log.d(TAG, "No secondary display available for dual-screen mode")
        }
    }

    /**
     * Dismisses the secondary presentation window.
     */
    fun dismissOuterScreen() {
        presentationWindow?.let {
            if (it.isShowing) {
                it.dismiss()
            }
        }
        presentationWindow = null
    }
}
