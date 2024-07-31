package com.jetbrains.kmpapp

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View

class GraphicOverlay(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private val lock = Any()
    private val graphics = mutableListOf<Graphic>()
    private var imageWidth: Int = 0
    private var imageHeight: Int = 0
    private var imageRotationDegrees: Int = 0
    var widthScaleFactor = 1.0f
    var heightScaleFactor = 1.0f
    var isFrontFacing = true

    fun setImageSourceInfo(width: Int, height: Int, rotationDegrees: Int, isFrontFacing: Boolean) {
        synchronized(lock) {
            imageWidth = width
            imageHeight = height
            imageRotationDegrees = rotationDegrees
            this.isFrontFacing = isFrontFacing

            // skala do kamery
            widthScaleFactor = width.toFloat() / this.width
            heightScaleFactor = height.toFloat() / this.height
        }
    }

    fun translateX(x: Float): Float {
        return if (isFrontFacing) {
            this.width - (x / widthScaleFactor)
        } else {
            x / widthScaleFactor
        }
    }

    fun translateY(y: Float): Float {
        return y / heightScaleFactor
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

    fun remove(graphic: Graphic) {
        synchronized(lock) {
            graphics.remove(graphic)
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
