package com.virtualcontroller.bt

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.virtualcontroller.R

class BluetoothHidService : Service() {

    private val binder = LocalBinder()
    lateinit var hidManager: BluetoothHidManager
        private set

    inner class LocalBinder : Binder() {
        fun getService(): BluetoothHidService = this@BluetoothHidService
    }

    override fun onCreate() {
        super.onCreate()
        hidManager = BluetoothHidManager(this)
        startForegroundServiceNotification()
        hidManager.initialize()
    }

    private fun startForegroundServiceNotification() {
        val channelId = "virtual_controller_bt_channel"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Virtual Gamepad Controller",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Active Bluetooth HID Gamepad Session"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Virtual Gamepad Active")
            .setContentText("Connected as Bluetooth Gamepad Peripheral")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onDestroy() {
        hidManager.unregister()
        super.onDestroy()
    }

    companion object {
        private const val NOTIFICATION_ID = 1001
    }
}
