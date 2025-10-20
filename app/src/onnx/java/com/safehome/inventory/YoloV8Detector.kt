package com.safehome.inventory

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import java.nio.FloatBuffer

class YoloV8Detector(context: Context) : ObjectDetector {
    private var session: OrtSession? = null
    private val ortEnv = OrtEnvironment.getEnvironment()
    private val confThreshold = 0.5f
    private val iouThreshold = 0.45f
    private val inputSize = 640

    init {
        try {
            val modelBytes = context.assets.open("yolov8n.onnx").readBytes()
            session = ortEnv.createSession(modelBytes)
            Log.d(TAG, "✓ YOLO model loaded successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error loading model", e)
        }
    }

    override fun detect(bitmap: Bitmap): List<Detection> {
        val startTime = System.currentTimeMillis()

        try {
            // Preprocess
            val inputTensor = preprocessImage(bitmap)

            // Run inference
            val inputs = mapOf(session!!.inputNames.iterator().next() to inputTensor)
            val output = session!!.run(inputs)

            // Parse output
            val outputTensor = output[0].value
            val detections = parseOutput(outputTensor)

            // Apply NMS
            val filtered = nonMaxSuppression(detections)

            output.close()

            val inferenceTime = System.currentTimeMillis() - startTime
            Log.d(TAG, "Inference: ${inferenceTime}ms, Detections: ${filtered.size}")

            return filtered
        } catch (e: Exception) {
            Log.e(TAG, "Detection error", e)
            return emptyList()
        }
    }

    private fun preprocessImage(bitmap: Bitmap): OnnxTensor {
        val resized = Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, true)
        val floatBuffer = FloatBuffer.allocate(3 * inputSize * inputSize)

        val pixels = IntArray(inputSize * inputSize)
        resized.getPixels(pixels, 0, inputSize, 0, 0, inputSize, inputSize)

        // RGB channels
        for (i in pixels.indices) {
            val pixel = pixels[i]
            floatBuffer.put(((pixel shr 16) and 0xFF) / 255.0f)
        }
        for (i in pixels.indices) {
            val pixel = pixels[i]
            floatBuffer.put(((pixel shr 8) and 0xFF) / 255.0f)
        }
        for (i in pixels.indices) {
            val pixel = pixels[i]
            floatBuffer.put((pixel and 0xFF) / 255.0f)
        }

        floatBuffer.rewind()
        return OnnxTensor.createTensor(ortEnv, floatBuffer, longArrayOf(1, 3, inputSize.toLong(), inputSize.toLong()))
    }

    private fun parseOutput(output: Any): List<Detection> {
        val detections = mutableListOf<Detection>()

        // YOLOv8 output shape: [1, 84, 8400]
        // ONNX returns: [batch=1][rows=84][cols=8400]
        // Each row is a FloatArray of 8400 detection values
        val data: Array<FloatArray> = when (output) {
            is Array<*> -> {
                val level1 = output[0] as Array<*>  // Remove batch dimension -> [84][8400]
                level1 as Array<FloatArray>         // Cast to typed array
            }
            else -> {
                Log.e(TAG, "Unexpected output type: ${output::class.java}")
                return emptyList()
            }
        }

        // data[row][col] where row=0-83, col=0-8399
        // Rows 0-3: bounding box (cx, cy, w, h)
        // Rows 4-83: class confidences (80 classes)
        for (i in 0 until 8400) {
            var maxConf = 0f
            var maxIdx = 0

            // Find max confidence across 80 classes (rows 4-83)
            for (j in 4 until 84) {
                val conf = data[j][i]
                if (conf > maxConf) {
                    maxConf = conf
                    maxIdx = j - 4
                }
            }

            if (maxConf > confThreshold) {
                val cx = data[0][i]
                val cy = data[1][i]
                val w = data[2][i]
                val h = data[3][i]

                detections.add(Detection(
                    classIndex = maxIdx,
                    className = CocoClasses.LABELS[maxIdx],
                    confidence = maxConf,
                    bbox = BoundingBox(cx - w/2, cy - h/2, cx + w/2, cy + h/2)
                ))
            }
        }

        return detections
    }

    private fun nonMaxSuppression(detections: List<Detection>): List<Detection> {
        val result = mutableListOf<Detection>()
        val sorted = detections.sortedByDescending { it.confidence }
        val suppressed = BooleanArray(sorted.size)

        for (i in sorted.indices) {
            if (suppressed[i]) continue
            result.add(sorted[i])

            for (j in i + 1 until sorted.size) {
                if (suppressed[j]) continue
                if (sorted[i].className == sorted[j].className) {
                    if (calculateIoU(sorted[i].bbox, sorted[j].bbox) > iouThreshold) {
                        suppressed[j] = true
                    }
                }
            }
        }

        return result
    }

    private fun calculateIoU(box1: BoundingBox, box2: BoundingBox): Float {
        val x1 = maxOf(box1.x1, box2.x1)
        val y1 = maxOf(box1.y1, box2.y1)
        val x2 = minOf(box1.x2, box2.x2)
        val y2 = minOf(box1.y2, box2.y2)

        val intersection = maxOf(0f, x2 - x1) * maxOf(0f, y2 - y1)
        val area1 = (box1.x2 - box1.x1) * (box1.y2 - box1.y1)
        val area2 = (box2.x2 - box2.x1) * (box2.y2 - box2.y1)

        return intersection / (area1 + area2 - intersection)
    }

    override fun close() {
        session?.close()
    }

    companion object {
        private const val TAG = "YoloV8Detector"
    }
}
