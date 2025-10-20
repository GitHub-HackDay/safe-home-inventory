package com.safehome.inventory

import android.app.Activity
import android.content.Context
import android.content.Intent

/**
 * Factory for creating the ExecuTorch-based detector.
 * This is the ExecuTorch flavor implementation.
 */
object DetectorFactory : PresentationProvider {
    fun createDetector(context: Context): ObjectDetector {
        return ExecuTorchDetector(context)
    }

    override fun getPresentationIntent(context: Context): Intent {
        return Intent(context, ExecuTorchPresentationActivity::class.java)
    }

    /**
     * Optional post-initialization hook for ExecuTorch-specific UI
     */
    fun onDetectorCreated(activity: Activity, detector: ObjectDetector) {
        if (activity is MainActivity) {
            activity.setupHardwareBanner(detector)
        }
    }
}
