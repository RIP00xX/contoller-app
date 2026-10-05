package com.virtualcontroller.foldable

import android.app.Presentation
import android.content.Context
import android.os.Bundle
import android.view.Display
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner

/**
 * Secondary Presentation Window projecting controller elements onto the Outer Cover Screen.
 * Configured with FLAG_NOT_FOCUSABLE to allow simultaneous active touch processing
 * alongside the primary inner screen.
 */
class OuterScreenPresentation(
    private val outerContext: Context,
    display: Display,

    private val content: @Composable () -> Unit
) : Presentation(outerContext, display) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        // Configure Window for Concurrent Multi-Display Touch Registration & Screen Backlight Activation
        window?.apply {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY)
            }
            addFlags(
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
            )
            clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        }



        val composeView = ComposeView(context).apply {
            (outerContext as? androidx.lifecycle.LifecycleOwner)?.let { owner ->
                setViewTreeLifecycleOwner(owner)
            }
            (outerContext as? androidx.savedstate.SavedStateRegistryOwner)?.let { owner ->
                setViewTreeSavedStateRegistryOwner(owner)
            }
            setContent {
                content()
            }
        }

        setContentView(composeView)
    }
}
