package com.safehome.inventory

import android.content.Context

/**
 * Factory for creating the ONNX-based detector.
 * This is the ONNX Runtime flavor implementation.
 */
object DetectorFactory {
    fun createDetector(context: Context): ObjectDetector {
        return YoloV8Detector(context)
    }
}
