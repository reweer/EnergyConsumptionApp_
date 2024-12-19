package com.jetbrains.kmpapp

import android.content.Context
import android.util.Size
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseDetector
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
import android.util.Log
import androidx.annotation.OptIn
import androidx.compose.runtime.LaunchedEffect
import android.graphics.Bitmap
import android.net.Uri
import com.google.mlkit.vision.pose.PoseLandmark
import java.io.IOException
import java.nio.ByteBuffer
import android.widget.FrameLayout



@OptIn(ExperimentalGetImage::class)
@Composable
actual fun CameraView() {
    val context = LocalContext.current

    // battery status logging
    LaunchedEffect(true) {
        startBatteryStatusLogging(context)
    }

    val lifecycleOwner = LocalLifecycleOwner.current

    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val previewView = remember { PreviewView(context) }
    val graphicOverlay = remember { GraphicOverlay(context, null) }

    // UI
    AndroidView(
        factory = {

            android.widget.FrameLayout(context).apply {

                addView(previewView, android.widget.FrameLayout.LayoutParams(
                    android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                    android.widget.FrameLayout.LayoutParams.MATCH_PARENT
                ))


                addView(graphicOverlay, android.widget.FrameLayout.LayoutParams(
                    android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                    android.widget.FrameLayout.LayoutParams.MATCH_PARENT
                ))
            }
        },
        modifier = Modifier.fillMaxSize(),
        update = {
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().apply {
                setSurfaceProvider(previewView.surfaceProvider)
            }

            graphicOverlay.setPreviewView(previewView)

            // Set up pose detector
            val poseDetectorOptions = PoseDetectorOptions.Builder()
                .setDetectorMode(PoseDetectorOptions.STREAM_MODE) // Real-time
                .build()
            val poseDetector = PoseDetection.getClient(poseDetectorOptions)

            // Configure ImageAnalysis
            val imageAnalysis = ImageAnalysis.Builder()
                .setTargetResolution(Size(640, 480)) // resolution
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            // procesowanie frame'ow przez ML Kit Pose Detector
            imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(context)) { imageProxy ->
                processImageProxy(poseDetector, imageProxy, graphicOverlay)
            }

            // bindowanie kamery
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageAnalysis
                )
            } catch (exc: Exception) {
                Log.e("CameraView", "Use case binding failed", exc)
            }
        }
    )
}









@OptIn(ExperimentalGetImage::class)
fun processImageProxy(poseDetector: PoseDetector, imageProxy: ImageProxy, graphicOverlay: GraphicOverlay) {
    val mediaImage = imageProxy.image
    if (mediaImage != null) {
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

        // przekazywanie rozmiarow wejsciowego orazu do graphicoverlay
        graphicOverlay.setImageSourceInfo(
            mediaImage.width, // Camera image width
            mediaImage.height, // Camera image height
            isFrontFacing = false // Back camera
        )

        poseDetector.process(image)
            .addOnSuccessListener { pose: Pose ->
                // zupdate'owanie graphic overlay
                graphicOverlay.clear()
                graphicOverlay.add(PoseGraphic(graphicOverlay, pose))
                graphicOverlay.postInvalidate()
            }
            .addOnFailureListener { e ->
                Log.e("PoseDetection", "Pose detection failed: ${e.message}")
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    } else {
        imageProxy.close()
    }
}







fun handlePoseDetectionSuccess(pose: Pose) {
    // punkty kluczowe
    val allPoseLandmarks = pose.allPoseLandmarks
    if (allPoseLandmarks.isNotEmpty()) {
        Log.d("PoseDetection", "Pose detected with ${allPoseLandmarks.size} landmarks.")
        //
        val leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)
        val rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)
        val leftElbow = pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW)
        val rightElbow = pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW)

        if (leftShoulder != null && rightShoulder != null) {
            Log.d("PoseDetection", "Left Shoulder: ${leftShoulder.position}, Right Shoulder: ${rightShoulder.position}")
        }

        if (leftElbow != null && rightElbow != null) {
            Log.d("PoseDetection", "Left Elbow: ${leftElbow.position}, Right Elbow: ${rightElbow.position}")
        }
        // miejsce na kolejne pozy
    } else {
        Log.d("PoseDetection", "No pose detected.")
    }
}

fun handlePoseDetectionFailure(e: Exception) {
    // Obsługa błędu detekcji
    Log.e("PoseDetection", "Pose detection failed: ${e.message}")
}

// Te inne przyklady przetwarzania wymienone w MLKit
fun processImageFromUri(context: Context, uri: Uri) {
    val poseDetectorOptions = PoseDetectorOptions.Builder()
        .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
        .build()

    val poseDetector = PoseDetection.getClient(poseDetectorOptions)
    val image: InputImage
    try {
        image = InputImage.fromFilePath(context, uri)
        poseDetector.process(image)
            .addOnSuccessListener { pose: Pose ->
                handlePoseDetectionSuccess(pose)
            }
            .addOnFailureListener { e ->
                handlePoseDetectionFailure(e)
            }
    } catch (e: IOException) {
        e.printStackTrace()
    }
}

fun processImageFromBitmap(bitmap: Bitmap) {
    val poseDetectorOptions = PoseDetectorOptions.Builder()
        .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
        .build()

    val poseDetector = PoseDetection.getClient(poseDetectorOptions)
    val image = InputImage.fromBitmap(bitmap, 0)
    poseDetector.process(image)
        .addOnSuccessListener { pose: Pose ->
            handlePoseDetectionSuccess(pose)
        }
        .addOnFailureListener { e ->
            handlePoseDetectionFailure(e)
        }
}

fun processImageFromByteBuffer(byteBuffer: ByteBuffer, width: Int, height: Int, rotationDegrees: Int) {
    val poseDetectorOptions = PoseDetectorOptions.Builder()
        .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
        .build()

    val poseDetector = PoseDetection.getClient(poseDetectorOptions)
    val image = InputImage.fromByteBuffer(
        byteBuffer,
        width,
        height,
        rotationDegrees,
        InputImage.IMAGE_FORMAT_NV21 // lub IMAGE_FORMAT_YV12
    )
    poseDetector.process(image)
        .addOnSuccessListener { pose: Pose ->
            handlePoseDetectionSuccess(pose)
        }
        .addOnFailureListener { e ->
            handlePoseDetectionFailure(e)
        }
}