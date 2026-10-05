package com.virtualcontroller.foldable

import android.app.Presentation
import android.content.Context
import android.os.Bundle
import android.view.Display
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner

internal fun createOuterComposeView(
    displayContext: Context,
    owner: ComponentActivity,
    content: @Composable () -> Unit
): ComposeView = ComposeView(displayContext).apply {
    // A display Context is not a LifecycleOwner. Pass the Activity explicitly.
    setViewTreeLifecycleOwner(owner)
    setViewTreeViewModelStoreOwner(owner)
    setViewTreeSavedStateRegistryOwner(owner)
    setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool)
    setContent(content)
}

class OuterScreenPresentation(
    private val owner: ComponentActivity,
    display: Display,
    private val content: @Composable () -> Unit
) : Presentation(owner, display) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window?.apply {
            // Keep the Presentation type; an overlay cannot activate a disabled panel.
            addFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        }
        setContentView(createOuterComposeView(context, owner, content))
    }

    override fun onStart() {
        super.onStart()
        window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
    }
}
