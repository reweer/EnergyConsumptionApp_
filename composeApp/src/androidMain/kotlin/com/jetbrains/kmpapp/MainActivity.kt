package com.jetbrains.kmpapp

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.content.ContextCompat.getContextForLanguage
import androidx.core.content.ContextCompat.startActivity

class MainActivity : ComponentActivity() {

    private val viewModel: PoseDetectionViewModelImpl by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainScreen(viewModel)
        }
        checkAndRequestPermissions()
    }

    private fun checkAndRequestPermissions() {
        if (checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(android.Manifest.permission.CAMERA), 101)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101 && grantResults.isNotEmpty() && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            showPermissionDeniedDialog(this)
        }
    }
}

private fun showPermissionDeniedDialog(context: Context) {
    val builder = AlertDialog.Builder(context)
    builder.setTitle("cmon bruh")
    builder.setMessage("we won't stalk u : )")

    builder.setNegativeButton("Cancel") { dialog, _ ->
        dialog.dismiss()
    }
    builder.show()
}