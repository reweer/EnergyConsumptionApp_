package com.jetbrains.kmpapp

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import androidx.camera.view.PreviewView



class GraphicOverlay(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private val lock = Any()
    private val graphics = mutableListOf<Graphic>()
    private var imageWidth: Int = 0
    private var imageHeight: Int = 0
    private var isFrontFacing = false
    private var scaleFactor = 1.0f
    private var offsetX = 0.0f
    private var offsetY = 0.0f
    private var previewView: PreviewView? = null

    fun setPreviewView(previewView: PreviewView) {
        this.previewView = previewView
    }

    fun setImageSourceInfo(width: Int, height: Int, isFrontFacing: Boolean) {
        synchronized(lock) {
            imageWidth = width
            imageHeight = height
            this.isFrontFacing = isFrontFacing

            val viewAspectRatio = this.width.toFloat() / this.height.toFloat()
            val imageAspectRatio = imageWidth.toFloat() / imageHeight.toFloat()

            if (viewAspectRatio > imageAspectRatio) {
                // View is wider than the image
                scaleFactor = this.height.toFloat() / imageHeight.toFloat()
                offsetX = (this.width - imageWidth * scaleFactor) / 2f
                offsetY = 0f
            } else {
                // View is taller than the image
                scaleFactor = this.width.toFloat() / imageWidth.toFloat()
                offsetX = 0f
                offsetY = (this.height - imageHeight * scaleFactor) / 2f
            }
        }
    }



    fun translateX(x: Float): Float {
        val centerX = this.width / 1.5f // Center of the overlay
        return centerX + (x * scaleFactor - offsetX - centerX) * 1.5f + 80 // Scale relative to the center
    }

    fun translateY(y: Float): Float {
        val centerY = this.height / 2.0f // Center of the overlay
        return centerY + (y * scaleFactor + offsetY - centerY) * 1.5f // Scale relative to the center
    }





    abstract class Graphic(protected val overlay: GraphicOverlay) {
        abstract fun draw(canvas: Canvas)
    }

    fun add(graphic: Graphic) {
        synchronized(lock) {
            graphics.add(graphic)
        }
        postInvalidate()
    }

    fun clear() {
        synchronized(lock) {
            graphics.clear()
        }
        postInvalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        synchronized(lock) {
            for (graphic in graphics) {
                graphic.draw(canvas)
            }
        }
    }
}
