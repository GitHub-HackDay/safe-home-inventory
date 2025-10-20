package com.safehome.inventory

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class OverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var detections: List<Detection> = emptyList()

    private val boxPaint = Paint().apply {
        color = Color.GREEN
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }

    private val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = 36f
    }

    private val bgPaint = Paint().apply {
        color = Color.argb(180, 0, 0, 0)
    }

    fun updateDetections(detections: List<Detection>) {
        this.detections = detections
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        for (detection in detections) {
            val bbox = detection.bbox
            val left = bbox.x1 * width / 640
            val top = bbox.y1 * height / 640
            val right = bbox.x2 * width / 640
            val bottom = bbox.y2 * height / 640

            canvas.drawRect(left, top, right, bottom, boxPaint)

            val label = "${detection.className} ${(detection.confidence * 100).toInt()}%"
            canvas.drawRect(left, top - 40f, left + label.length * 20f, top, bgPaint)
            canvas.drawText(label, left + 4f, top - 8f, textPaint)
        }
    }
}
