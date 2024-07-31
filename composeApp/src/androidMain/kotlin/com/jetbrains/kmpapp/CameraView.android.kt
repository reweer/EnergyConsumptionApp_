package com.jetbrains.kmpapp

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
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
import com.google.mlkit.vision.pose.PoseLandmark
import java.io.IOException
import java.nio.ByteBuffer
import android.util.Log
import androidx.annotation.OptIn

@OptIn(ExperimentalGetImage::class)
@Composable
actual fun CameraView() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val previewView = remember { PreviewView(context) }
    val graphicOverlay = remember { GraphicOverlay(context, null) }

    AndroidView(factory = { previewView },
        modifier = Modifier.fillMaxSize(),
        update = {
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().apply {
                setSurfaceProvider(previewView.surfaceProvider)
            }

            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
            val poseDetectorOptions = PoseDetectorOptions.Builder()
                .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
                .build()
            val poseDetector = PoseDetection.getClient(poseDetectorOptions)

            val imageAnalysis = ImageAnalysis.Builder()
                .setTargetResolution(Size(640, 480))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(context)) { imageProxy ->
                val rotationDegrees = imageProxy.imageInfo.rotationDegrees
                val image = InputImage.fromMediaImage(imageProxy.image!!, rotationDegrees)
                poseDetector.process(image)
                    .addOnSuccessListener { pose ->
                        graphicOverlay.setImageSourceInfo(imageProxy.width, imageProxy.height, rotationDegrees, isFrontFacing = true)
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
            }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner, cameraSelector, preview, imageAnalysis
                )
            } catch (exc: Exception) {
                Log.e("CameraView", "Use case binding failed", exc)
            }
        }
    )

    AndroidView(factory = { graphicOverlay }, modifier = Modifier.fillMaxSize())
}


@OptIn(ExperimentalGetImage::class)
fun processImageProxy(poseDetector: PoseDetector, imageProxy: ImageProxy, graphicOverlay: GraphicOverlay) {
    val mediaImage = imageProxy.image
    if (mediaImage != null) {
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        poseDetector.process(image)
            .addOnSuccessListener { pose: Pose ->
                graphicOverlay.clear()
                graphicOverlay.add(PoseGraphic(graphicOverlay, pose))
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