package com.safehome.inventory

import android.content.Context

/**
 * Factory for creating the ExecuTorch-based detector.
 * This is the ExecuTorch flavor implementation.
 */
object DetectorFactory {
    fun createDetector(context: Context): ObjectDetector {
        return ExecuTorchDetector(context)
    }
}
