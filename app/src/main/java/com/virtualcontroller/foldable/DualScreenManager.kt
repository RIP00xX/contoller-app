package com.virtualcontroller.foldable

import android.content.Context
import android.hardware.display.DisplayManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Display
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.window.area.WindowAreaCapability
import androidx.window.area.WindowAreaController
import androidx.window.area.WindowAreaInfo
import androidx.window.area.WindowAreaPresentationSessionCallback
import androidx.window.area.WindowAreaSessionPresenter
import androidx.window.core.ExperimentalWindowApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class OuterScreenStatus(
    val message: String = "Checking cover-screen support…",
    val canStart: Boolean = false,
    val active: Boolean = false
)

/** A Presentation alone cannot power on a firmware-disabled cover panel. */
@OptIn(ExperimentalWindowApi::class)
class DualScreenManager(private val activity: ComponentActivity) {
    private val displayManager = activity.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
    private val areaController = WindowAreaController.getOrCreate()
    private val operation = WindowAreaCapability.Operation.OPERATION_PRESENT_ON_AREA
    private var rearArea: WindowAreaInfo? = null
    private var session: WindowAreaSessionPresenter? = null
    private var presentation: OuterScreenPresentation? = null
    private var pending = false
    private var generation = 0
    private var content by mutableStateOf<(@Composable () -> Unit)?>(null)
    private val mutableStatus = MutableStateFlow(OuterScreenStatus())
    val status = mutableStatus.asStateFlow()

    private val displayListener = object : DisplayManager.DisplayListener {
        override fun onDisplayAdded(displayId: Int) = displaysChanged()
        override fun onDisplayRemoved(displayId: Int) = displaysChanged()
        override fun onDisplayChanged(displayId: Int) = displaysChanged()
    }

    init {
        Log.i(TAG, "Device=${Build.MANUFACTURER} ${Build.MODEL}, SDK=${Build.VERSION.SDK_INT}, firmware=${Build.DISPLAY}")
        activity.lifecycleScope.launch {
            activity.repeatOnLifecycle(Lifecycle.State.STARTED) {
                displayManager.registerDisplayListener(displayListener, Handler(Looper.getMainLooper()))
                try {
                    logDisplays()
                    areaController.windowAreaInfos.collect { areas ->
                        rearArea = areas.firstOrNull { it.type == WindowAreaInfo.Type.TYPE_REAR_FACING }
                        Log.i(TAG, "Rear presentation capability=${rearArea?.getCapability(operation)?.status}, areas=${areas.size}")
                        refreshStatus()
                    }
                } finally {
                    displayManager.unregisterDisplayListener(displayListener)
                }
            }
        }
    }

    fun setOuterScreenContent(newContent: @Composable () -> Unit) {
        content = newContent
    }

    /** Start from a user action: the firmware may show a system consent dialog. */
    fun showOuterScreenContent() {
        if (session != null || presentation != null || pending || content == null) return
        logDisplays()
        val area = rearArea
        if (area?.getCapability(operation)?.status == WindowAreaCapability.Status.WINDOW_AREA_STATUS_AVAILABLE) {
            pending = true
            mutableStatus.value = OuterScreenStatus("Waiting for dual-screen session…")
            val requestGeneration = ++generation
            try {
                areaController.presentContentOnWindowArea(
                    area.token, activity, ContextCompat.getMainExecutor(activity),
                    object : WindowAreaPresentationSessionCallback {
                        override fun onSessionStarted(session: WindowAreaSessionPresenter) {
                            if (generation != requestGeneration) {
                                session.close()
                                return
                            }
                            pending = false
                            this@DualScreenManager.session = session
                            try {
                                mutableStatus.value = OuterScreenStatus("Cover session started; waiting for visibility…")
                                session.setContentView(createOuterComposeView(session.context, activity) { content?.invoke() })
                            } catch (e: Exception) {
                                dismissOuterScreen()
                                reportError(e)
                            }
                        }
                        override fun onSessionEnded(t: Throwable?) {
                            if (generation != requestGeneration) return
                            pending = false
                            this@DualScreenManager.session = null
                            refreshStatus()
                            if (t != null) reportError(t)
                        }
                        override fun onContainerVisibilityChanged(isVisible: Boolean) {
                            if (generation != requestGeneration) return
                            Log.i(TAG, "Cover content visible=$isVisible")
                            mutableStatus.value = OuterScreenStatus(
                                if (isVisible) "Cover screen active" else "Cover session hidden", active = isVisible
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                pending = false
                reportError(e)
            }
            return
        }
        // Use only displays advertised for Presentation, never guess by a nonzero ID.
        val display = presentationDisplay()
        if (display == null) {
            refreshStatus()
            return
        }
        try {
            val window = OuterScreenPresentation(activity, display) { content?.invoke() }
            window.setOnDismissListener {
                presentation = null
                refreshStatus()
            }
            presentation = window
            window.show()
            mutableStatus.value = OuterScreenStatus("Secondary display active (${display.displayId})", active = true)
        } catch (e: Exception) {
            presentation = null
            reportError(e)
        }
    }

    fun dismissOuterScreen() {
        generation++
        pending = false
        val oldSession = session
        session = null
        oldSession?.close()
        val oldPresentation = presentation
        presentation = null
        oldPresentation?.dismiss()
        refreshStatus()
    }

    private fun presentationDisplay(): Display? = displayManager
        .getDisplays(DisplayManager.DISPLAY_CATEGORY_PRESENTATION)
        .firstOrNull { it.displayId != activity.windowManager.defaultDisplay.displayId && it.isValid && it.state == Display.STATE_ON }

    private fun refreshStatus() {
        if (session != null || presentation != null || pending) return
        val capability = rearArea?.getCapability(operation)?.status
        mutableStatus.value = when {
            capability == WindowAreaCapability.Status.WINDOW_AREA_STATUS_AVAILABLE ->
                OuterScreenStatus("Cover screen ready", canStart = true)
            presentationDisplay() != null -> OuterScreenStatus("Secondary display ready", canStart = true)
            capability == WindowAreaCapability.Status.WINDOW_AREA_STATUS_UNAVAILABLE ->
                OuterScreenStatus("Cover screen unavailable in current device state")
            capability == WindowAreaCapability.Status.WINDOW_AREA_STATUS_ACTIVE ->
                OuterScreenStatus("Cover screen in use by another session")
            else -> OuterScreenStatus("Firmware does not expose dual-screen mode to this app")
        }
    }

    private fun logDisplays() {
        displayManager.displays.forEach {
            Log.i(TAG, "Display id=${it.displayId}, name=${it.name}, state=${it.state}, flags=0x${it.flags.toString(16)}, valid=${it.isValid}")
        }
    }

    private fun displaysChanged() {
        logDisplays()
        refreshStatus()
    }

    private fun reportError(error: Throwable) {
        Log.e(TAG, "Outer screen failed", error)
        mutableStatus.value = OuterScreenStatus("Outer screen failed: ${error.javaClass.simpleName}", canStart = true)
    }

    companion object { private const val TAG = "DualScreenManager" }
}
