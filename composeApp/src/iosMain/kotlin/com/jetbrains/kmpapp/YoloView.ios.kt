package com.jetbrains.kmpapp

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.CValue
import platform.AVFoundation.*
import platform.CoreGraphics.CGRect
import platform.QuartzCore.CATransaction
import platform.QuartzCore.kCATransactionDisableActions
import platform.UIKit.UIView
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.objcPtr
import platform.Foundation.NSError
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun YoloView() {
    val cameraPreviewLayer = remember { AVCaptureVideoPreviewLayer() }

    UIKitView(
        modifier = Modifier.fillMaxSize(),
        background = Color.Black,
        factory = {
            val container = UIView()
            dispatch_async(dispatch_get_main_queue()) {
                setupCameraPreviewLayer(cameraPreviewLayer, container)
            }
            container
        },
        onResize = { container: UIView, rect: CValue<CGRect> ->
            CATransaction.begin()
            CATransaction.setValue(true, kCATransactionDisableActions)
            container.layer.setFrame(rect)
            cameraPreviewLayer.setFrame(rect)
            CATransaction.commit()
        }
    )
}

@OptIn(ExperimentalForeignApi::class)
private fun setupCameraPreviewLayer(
    cameraPreviewLayer: AVCaptureVideoPreviewLayer,
    container: UIView
) {
    val device = AVCaptureDevice.devicesWithMediaType(AVMediaTypeVideo)
        .filterIsInstance<AVCaptureDevice>()
        .firstOrNull { it.position == AVCaptureDevicePositionFront }

    if (device == null) {
        println("No front camera found")
        return
    }

    val input: AVCaptureDeviceInput
    try {
        input = AVCaptureDeviceInput.deviceInputWithDevice(device, null) as AVCaptureDeviceInput
    } catch (e: Throwable) {
        println("Error creating device input: ${e.message}")
        return
    }

    val output = AVCaptureStillImageOutput()
    output.outputSettings = mapOf(AVVideoCodecKey to AVVideoCodecJPEG)

    val session = AVCaptureSession()
    session.sessionPreset = AVCaptureSessionPresetPhoto

    if (session.canAddInput(input)) {
        session.addInput(input)
    } else {
        println("Cannot add input to session")
        return
    }

    if (session.canAddOutput(output)) {
        session.addOutput(output)
    } else {
        println("Cannot add output to session")
        return
    }

    cameraPreviewLayer.session = session
    cameraPreviewLayer.videoGravity = AVLayerVideoGravityResizeAspectFill
    container.layer.addSublayer(cameraPreviewLayer)

    session.startRunning()
}
