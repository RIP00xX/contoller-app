package com.virtualcontroller.bt

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHidDevice
import android.bluetooth.BluetoothHidDeviceAppQosSettings
import android.bluetooth.BluetoothHidDeviceAppSdpSettings
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.Executors

/**
 * State representing Bluetooth HID Device connection status.
 */
sealed class HidConnectionState {
    object Idle : HidConnectionState()
    object Initializing : HidConnectionState()
    object Registered : HidConnectionState()
    data class Connected(val device: BluetoothDevice, val deviceName: String) : HidConnectionState()
    data class Error(val message: String) : HidConnectionState()
}

/**
 * Low-latency Bluetooth HID Device API manager.
 * Registers Android phone as a native Bluetooth Gamepad peripheral.
 */
@SuppressLint("MissingPermission")
class BluetoothHidManager(private val context: Context) {

    private val TAG = "BluetoothHidManager"

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    @Volatile private var hidDevice: BluetoothHidDevice? = null
    @Volatile private var hostDevice: BluetoothDevice? = null
    @Volatile private var lastGamepadReport = GamepadReport()
    @Volatile private var lastTouchpadReport = TouchpadReport()

    private val _connectionState = MutableStateFlow<HidConnectionState>(HidConnectionState.Idle)
    val connectionState: StateFlow<HidConnectionState> = _connectionState.asStateFlow()

    private val executor = Executors.newSingleThreadExecutor()

    private val serviceListener = object : BluetoothProfile.ServiceListener {
        override fun onServiceConnected(profile: Int, proxy: BluetoothProfile?) {
            if (profile == BluetoothProfile.HID_DEVICE) {
                Log.d(TAG, "HID Device Profile Connected")
                hidDevice = proxy as? BluetoothHidDevice
                registerAppSdpSettings()
            }
        }

        override fun onServiceDisconnected(profile: Int) {
            if (profile == BluetoothProfile.HID_DEVICE) {
                Log.d(TAG, "HID Device Profile Disconnected")
                hidDevice = null
                hostDevice = null
                _connectionState.value = HidConnectionState.Idle
            }
        }
    }

    private val hidCallback = object : BluetoothHidDevice.Callback() {
        override fun onAppStatusChanged(pluggedDevice: BluetoothDevice?, registered: Boolean) {
            Log.d(TAG, "onAppStatusChanged: registered=$registered, pluggedDevice=${pluggedDevice?.name}")
            if (registered) {
                _connectionState.value = HidConnectionState.Registered
                pluggedDevice?.let { device ->
                    hostDevice = device
                    _connectionState.value = HidConnectionState.Connected(device, device.name ?: device.address)
                }
            } else {
                hostDevice = null
                _connectionState.value = HidConnectionState.Idle
            }
        }

        override fun onConnectionStateChanged(device: BluetoothDevice?, state: Int) {
            Log.d(TAG, "onConnectionStateChanged: device=${device?.name}, state=$state")
            when (state) {
                BluetoothProfile.STATE_CONNECTED -> {
                    if (device != null) {
                        hostDevice = device
                        _connectionState.value = HidConnectionState.Connected(device, device.name ?: device.address)
                    }
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    if (device == hostDevice) {
                        hostDevice = null
                        lastGamepadReport = GamepadReport()
                        lastTouchpadReport = TouchpadReport()
                        _connectionState.value = HidConnectionState.Registered
                    }
                }
            }
        }

        override fun onGetReport(device: BluetoothDevice?, type: Byte, id: Byte, bufferSize: Int) {
            Log.d(TAG, "onGetReport requested")
            if (device == null) return
            if (type == BluetoothHidDevice.REPORT_TYPE_FEATURE && id == HidReportDescriptor.REPORT_ID_TOUCHPAD_CAPABILITIES) {
                hidDevice?.replyReport(device, type, id, byteArrayOf(1))
                return
            }
            if (type != BluetoothHidDevice.REPORT_TYPE_INPUT) {
                hidDevice?.reportError(device, BluetoothHidDevice.ERROR_RSP_UNSUPPORTED_REQ)
                return
            }
            val data = when (id) {
                HidReportDescriptor.REPORT_ID_GAMEPAD -> lastGamepadReport.toByteArray()
                HidReportDescriptor.REPORT_ID_TOUCHPAD -> lastTouchpadReport.toByteArray()
                else -> {
                    hidDevice?.reportError(device, BluetoothHidDevice.ERROR_RSP_INVALID_RPT_ID)
                    return
                }
            }
            if (bufferSize > 0 && bufferSize < data.size) {
                hidDevice?.reportError(device, BluetoothHidDevice.ERROR_RSP_INVALID_PARAM)
                return
            }
            hidDevice?.replyReport(device, type, id, data)
        }

        override fun onSetReport(device: BluetoothDevice?, type: Byte, id: Byte, data: ByteArray?) {
            Log.d(TAG, "onSetReport received from host")
            if (device != null) hidDevice?.reportError(device, BluetoothHidDevice.ERROR_RSP_UNSUPPORTED_REQ)
        }
    }

    /**
     * Initializes Bluetooth HID profile proxy.
     */
    fun initialize() {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            _connectionState.value = HidConnectionState.Error("Bluetooth is not enabled")
            return
        }
        _connectionState.value = HidConnectionState.Initializing
        bluetoothAdapter.getProfileProxy(context, serviceListener, BluetoothProfile.HID_DEVICE)
    }

    /**
     * Registers Gamepad SDP record with Android Bluetooth Stack.
     */
    private fun registerAppSdpSettings() {
        val sdpSettings = BluetoothHidDeviceAppSdpSettings(
            "Virtual Gamepad",
            "Foldable Bluetooth Controller",
            "Antigravity",
            BluetoothHidDevice.SUBCLASS2_GAMEPAD,
            HidReportDescriptor.CONTROLLER_DESCRIPTOR
        )

        val qosSettings = BluetoothHidDeviceAppQosSettings(
            BluetoothHidDeviceAppQosSettings.SERVICE_BEST_EFFORT,
            800,
            900,
            0,
            11250,
            11250
        )

        try {
            val accepted = hidDevice?.registerApp(
                sdpSettings,
                qosSettings,
                null,
                executor,
                hidCallback
            )
            if (accepted != true) _connectionState.value = HidConnectionState.Error("HID registration was rejected")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to register HID App SDP", e)
            _connectionState.value = HidConnectionState.Error("SDP Register Error: ${e.localizedMessage}")
        }
    }

    /**
     * Sends low-latency input report byte array to connected host device.
     */
    fun sendReport(report: GamepadReport) {
        lastGamepadReport = report
        val device = hostDevice
        val proxy = hidDevice
        if (proxy != null && device != null) {
            val reportBytes = report.toByteArray()
            proxy.sendReport(device, HidReportDescriptor.REPORT_ID_GAMEPAD.toInt(), reportBytes)
        }
    }

    fun sendTouchpadReport(report: TouchpadReport) {
        lastTouchpadReport = report
        val device = hostDevice ?: return
        hidDevice?.sendReport(device, HidReportDescriptor.REPORT_ID_TOUCHPAD.toInt(), report.toByteArray())
    }

    /**
     * Unregisters SDP record and closes profile proxy.
     */
    fun unregister() {
        try {
            hidDevice?.unregisterApp()
            bluetoothAdapter?.closeProfileProxy(BluetoothProfile.HID_DEVICE, hidDevice)
        } catch (e: Exception) {
            Log.e(TAG, "Error unregistering HID", e)
        } finally {
            hostDevice = null
            hidDevice = null
            executor.shutdown()
        }
    }
}
