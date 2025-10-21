package com.safehome.inventory

import android.app.Activity
import android.content.Context
import android.content.Intent

/**
 * Factory for creating the QNN-based detector.
 * This is the QNN flavor implementation with direct Qualcomm backend.
 */
object DetectorFactory : PresentationProvider {
    fun createDetector(context: Context): ObjectDetector {
        return QnnDetector(context)
    }

    override fun getPresentationIntent(context: Context): Intent {
        return Intent(context, QnnPresentationActivity::class.java)
    }

    /**
     * Optional post-initialization hook for QNN-specific UI
     */
    fun onDetectorCreated(activity: Activity, detector: ObjectDetector) {
        if (activity is MainActivity) {
            activity.setupHardwareBanner(detector)
            activity.setupAIVisionHighlight()
        }
    }
}
