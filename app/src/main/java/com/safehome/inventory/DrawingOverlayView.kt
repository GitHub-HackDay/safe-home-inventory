package com.safehome.inventory

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class DrawingOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val drawPaint = Paint().apply {
        color = Color.YELLOW
        style = Paint.Style.STROKE
        strokeWidth = 8f
        isAntiAlias = true
    }

    private val currentPath = Path()
    private val completedPaths = mutableListOf<Path>()
    private var isDrawingEnabled = false
    private var onDrawingComplete: ((Path) -> Unit)? = null

    private var startX = 0f
    private var startY = 0f

    fun setDrawingEnabled(enabled: Boolean) {
        isDrawingEnabled = enabled
        if (!enabled) {
            currentPath.reset()
            completedPaths.clear()
            invalidate()
        }
    }

    fun setOnDrawingCompleteListener(listener: (Path) -> Unit) {
        onDrawingComplete = listener
    }

    fun clearDrawing() {
        currentPath.reset()
        completedPaths.clear()
        invalidate()
    }

    /**
     * Get the bounding rectangle of the drawn path
     */
    fun getPathBounds(path: Path): android.graphics.RectF {
        val bounds = android.graphics.RectF()
        path.computeBounds(bounds, true)
        return bounds
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isDrawingEnabled) return false

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startX = event.x
                startY = event.y
                currentPath.reset()
                currentPath.moveTo(event.x, event.y)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                currentPath.lineTo(event.x, event.y)
                invalidate()
                return true
            }
            MotionEvent.ACTION_UP -> {
                currentPath.lineTo(event.x, event.y)
                // Close the path if it's close to the start (make a circle)
                if (isNearStart(event.x, event.y)) {
                    currentPath.close()
                }
                completedPaths.add(Path(currentPath))

                // Trigger callback
                onDrawingComplete?.invoke(Path(currentPath))

                currentPath.reset()
                invalidate()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun isNearStart(x: Float, y: Float): Boolean {
        val threshold = 50f
        val dx = x - startX
        val dy = y - startY
        return Math.sqrt((dx * dx + dy * dy).toDouble()) < threshold
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw completed paths
        for (path in completedPaths) {
            canvas.drawPath(path, drawPaint)
        }

        // Draw current path
        if (!currentPath.isEmpty) {
            canvas.drawPath(currentPath, drawPaint)
        }
    }
}
