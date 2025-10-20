package com.safehome.inventory

import android.graphics.Bitmap

/**
 * Common interface for object detection implementations.
 * This allows us to swap between ONNX and ExecuTorch runtimes.
 */
interface ObjectDetector {
    /**
     * Detect objects in the given bitmap image.
     * @param bitmap The image to analyze
     * @return List of detected objects with bounding boxes and classifications
     */
    fun detect(bitmap: Bitmap): List<Detection>

    /**
     * Get hardware accelerator information (optional).
     * @return Hardware info string, or null if not applicable
     */
    fun getHardwareInfo(): String? = null

    /**
     * Get last inference time in milliseconds (optional).
     * @return Inference time, or 0 if not tracked
     */
    fun getLastInferenceTime(): Long = 0

    /**
     * Clean up resources when done.
     */
    fun close()
}
