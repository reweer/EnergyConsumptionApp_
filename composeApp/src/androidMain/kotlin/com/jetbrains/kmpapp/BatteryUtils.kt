package com.jetbrains.kmpapp

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Handler
import android.os.Looper
import java.io.File
import java.io.FileOutputStream
import java.io.PrintWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class BatteryStatus(
    val isCharging: Boolean,
    val usbCharge: Boolean,
    val acCharge: Boolean,
    val batteryPct: Float?
)

fun getBatteryStatus(context: Context): BatteryStatus {
    val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
    val batteryStatus = context.registerReceiver(null, intentFilter)

    val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
    val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
    val chargePlug = batteryStatus?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) ?: -1
    val usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB
    val acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC

    val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
    val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
    val batteryPct = if (level >= 0 && scale > 0) (level.toFloat() / scale.toFloat()) * 100 else null

    return BatteryStatus(isCharging, usbCharge, acCharge, batteryPct)
}

fun logBatteryStatus(context: Context, batteryStatus: BatteryStatus) {
    val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
    val logMessage = "$timestamp - Charging: ${batteryStatus.isCharging}, USB Charge: ${batteryStatus.usbCharge}, AC Charge: ${batteryStatus.acCharge}, Battery Percentage: ${batteryStatus.batteryPct}"
    val file = File(context.getExternalFilesDir(null), "battery_log.txt")
    FileOutputStream(file, true).use { fos ->
        PrintWriter(fos).use { writer ->
            writer.println(logMessage)
        }
    }
}

fun startBatteryStatusLogging(context: Context) {
    val handler = Handler(Looper.getMainLooper())
    val runnable = object : Runnable {
        override fun run() {
            val batteryStatus = getBatteryStatus(context)
            logBatteryStatus(context, batteryStatus)
            handler.postDelayed(this, 180000) //3 min
        }
    }
    handler.post(runnable)
}
