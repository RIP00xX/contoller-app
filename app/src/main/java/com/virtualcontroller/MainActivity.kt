package com.virtualcontroller

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.virtualcontroller.bt.BluetoothHidService
import com.virtualcontroller.bt.GamepadReport
import com.virtualcontroller.bt.ControllerInputState
import com.virtualcontroller.bt.HidConnectionState
import com.virtualcontroller.data.ProfileRepository
import com.virtualcontroller.foldable.DevicePosture
import com.virtualcontroller.foldable.DualScreenManager
import com.virtualcontroller.foldable.FoldStateTracker
import com.virtualcontroller.haptics.HapticFeedbackManager
import com.virtualcontroller.model.TargetScreen
import com.virtualcontroller.ui.controller.MainControllerScreen
import com.virtualcontroller.ui.controller.OuterControllerScreen
import com.virtualcontroller.ui.editor.LayoutEditorScreen
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private var hidService: BluetoothHidService? = null
    private var isServiceBound by mutableStateOf(false)
    private var currentPosture by mutableStateOf(DevicePosture.CLOSED)
    private val inputState = ControllerInputState()

    private fun updateAndSendReport(updater: (GamepadReport) -> GamepadReport) {
        val updated = inputState.update(updater)
        hidService?.hidManager?.sendReport(updated)
    }

    private lateinit var foldStateTracker: FoldStateTracker
    private lateinit var dualScreenManager: DualScreenManager
    private lateinit var hapticManager: HapticFeedbackManager
    private lateinit var profileRepository: ProfileRepository

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            bindHidService()
        }
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as BluetoothHidService.LocalBinder
            hidService = binder.getService()
            isServiceBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            hidService = null
            isServiceBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        foldStateTracker = FoldStateTracker(this)
        dualScreenManager = DualScreenManager(this)
        hapticManager = HapticFeedbackManager(this)
        profileRepository = ProfileRepository(this)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                foldStateTracker.postureFlow.collect { currentPosture = it }
            }
        }

        checkPermissionsAndStartService()

        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color(0xFF090D16)
            ) {
                val outerScreenStatus by dualScreenManager.status.collectAsState()
                var connectionState by remember { mutableStateOf<HidConnectionState>(HidConnectionState.Idle) }

                val profiles by profileRepository.getSavedProfiles().collectAsState(initial = profileRepository.defaultProfiles)
                val activeProfileId by profileRepository.getActiveProfileId().collectAsState(initial = profileRepository.defaultProfiles.first().id)

                val activeProfile = profiles.find { it.id == activeProfileId } ?: profiles.first()
                var isEditMode by remember { mutableStateOf(false) }

                val scope = rememberCoroutineScope()

                // Update content without recreating an active dual-screen session.
                LaunchedEffect(activeProfile, isEditMode) {
                    updateAndSendReport { GamepadReport() }
                    val hasOuterElements = activeProfile.elements.any { it.targetScreen == TargetScreen.OUTER_SCREEN }
                    if (hasOuterElements && !isEditMode) {
                        dualScreenManager.setOuterScreenContent {
                            OuterControllerScreen(
                                currentProfile = activeProfile,
                                hapticManager = hapticManager,
                                onReportStateChanged = ::updateAndSendReport
                            )
                        }
                    } else {
                        dualScreenManager.dismissOuterScreen()
                    }
                }


                // Poll Connection State from Service
                DisposableEffect(isServiceBound) {
                    val job = scope.launch {
                        hidService?.hidManager?.connectionState?.collectLatest { state ->
                            connectionState = state
                        }
                    }
                    onDispose { job.cancel() }
                }

                if (isEditMode) {
                    LayoutEditorScreen(
                        profile = activeProfile,
                        onSaveProfile = { updated ->
                            scope.launch {
                                profileRepository.saveCustomProfile(updated)
                                isEditMode = false
                            }
                        },
                        onCancel = { isEditMode = false }
                    )
                } else {
                    MainControllerScreen(
                        currentProfile = activeProfile,
                        allProfiles = profiles,
                        connectionState = connectionState,
                        devicePosture = currentPosture,
                        outerScreenStatus = outerScreenStatus,
                        onEnableOuterScreen = { dualScreenManager.showOuterScreenContent() },
                        hapticManager = hapticManager,
                        onSelectProfile = { selected ->
                            scope.launch {
                                profileRepository.setActiveProfileId(selected.id)
                            }
                        },
                        onOpenEditor = { isEditMode = true },
                        onReportStateChanged = ::updateAndSendReport
                    )
                }
            }
        }
    }

    private fun checkPermissionsAndStartService() {
        val neededPermissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            neededPermissions.add(Manifest.permission.BLUETOOTH_CONNECT)
            neededPermissions.add(Manifest.permission.BLUETOOTH_ADVERTISE)
            neededPermissions.add(Manifest.permission.BLUETOOTH_SCAN)
        } else {
            neededPermissions.add(Manifest.permission.BLUETOOTH)
            neededPermissions.add(Manifest.permission.BLUETOOTH_ADMIN)
        }

        val missing = neededPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missing.isNotEmpty()) {
            permissionLauncher.launch(missing.toTypedArray())
        } else {
            bindHidService()
        }

    }


    private fun bindHidService() {
        val intent = Intent(this, BluetoothHidService::class.java)
        startService(intent)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onStop() {
        dualScreenManager.dismissOuterScreen()
        updateAndSendReport { GamepadReport() }
        super.onStop()
    }

    override fun onDestroy() {
        if (isServiceBound) {
            unbindService(serviceConnection)
            isServiceBound = false
        }
        dualScreenManager.dismissOuterScreen()
        super.onDestroy()
    }
}
