package com.jetbrains.kmpapp

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.google.mlkit.vision.pose.Pose

class PoseGraphic(overlay: GraphicOverlay, private val pose: Pose) : GraphicOverlay.Graphic(overlay) {
    private val paint = Paint().apply {
        color = Color.GREEN
        strokeWidth = 8.0f
    }

    override fun draw(canvas: Canvas) {
        for (landmark in pose.allPoseLandmarks) {
            val canvasX = overlay.translateX(landmark.position.x)
            val canvasY = overlay.translateY(landmark.position.y)
            canvas.drawCircle(canvasX, canvasY, 10.0f, paint)
        }
    }
}
