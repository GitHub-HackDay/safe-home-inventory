package com.safehome.inventory

import android.app.Activity
import android.content.Context
import android.content.Intent

/**
 * Factory for creating the ONNX-based detector.
 * This is the ONNX Runtime flavor implementation.
 */
object DetectorFactory : PresentationProvider {
    fun createDetector(context: Context): ObjectDetector {
        return YoloV8Detector(context)
    }

    override fun getPresentationIntent(context: Context): Intent {
        return Intent(context, OnnxPresentationActivity::class.java)
    }

    /**
     * Optional post-initialization hook (no-op for ONNX flavor)
     */
    fun onDetectorCreated(activity: Activity, detector: ObjectDetector) {
        // No extra UI for ONNX flavor
    }
}
