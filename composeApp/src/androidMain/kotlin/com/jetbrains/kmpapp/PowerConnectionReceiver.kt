package com.jetbrains.kmpapp


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class PowerConnectionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_POWER_CONNECTED -> {
                Log.d("PowerConnectionReceiver", "Device connected to power.")
            }
            Intent.ACTION_POWER_DISCONNECTED -> {
                Log.d("PowerConnectionReceiver", "Device disconnected from power.")
            }
        } //ni ma jak sprawdzic bo nie mam jak odpiac
    }
}
