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
     * Clean up resources when done.
     */
    fun close()
}
