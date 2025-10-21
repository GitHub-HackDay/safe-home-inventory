package com.safehome.inventory

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ExecuTorch-specific extensions for MainActivity.
 * Adds hardware acceleration banner and AI description features.
 */
fun MainActivity.setupHardwareBanner(detector: ObjectDetector) {
    // Only add banner if detector supports hardware info
    val hardwareInfo = detector.getHardwareInfo() ?: return

    val rootView = window.decorView.findViewById<FrameLayout>(android.R.id.content)

    // Inflate the hardware banner
    val banner = LayoutInflater.from(this).inflate(
        R.layout.hardware_banner,
        rootView,
        false
    )

    // Set hardware info text
    banner.findViewById<TextView>(R.id.hardwareInfoText)?.text = hardwareInfo

    // Update inference time periodically
    val inferenceTimeText = banner.findViewById<TextView>(R.id.inferenceTimeText)
    val updateInferenceTime = {
        val time = detector.getLastInferenceTime()
        if (time > 0) {
            inferenceTimeText?.text = "Inference: ~${time}ms"
        }
    }

    // Add banner to top of view hierarchy
    val layoutParams = FrameLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    ).apply {
        gravity = android.view.Gravity.BOTTOM
        setMargins(16, 0, 16, 300) // Position above inventory card
    }

    rootView.addView(banner, layoutParams)

    // Update inference time every 2 seconds
    val handler = android.os.Handler(mainLooper)
    val updateRunnable = object : Runnable {
        override fun run() {
            updateInferenceTime()
            handler.postDelayed(this, 2000)
        }
    }
    handler.post(updateRunnable)

    // Make banner clickable to show AI descriptions
    banner.setOnClickListener {
        showAIDescriptions()
    }
}

/**
 * Show AI-generated descriptions for inventory items (ExecuTorch + LLM feature)
 */
fun MainActivity.showAIDescriptions() {
    val inventoryManager = try {
        val field = this::class.java.getDeclaredField("inventoryManager")
        field.isAccessible = true
        field.get(this) as? InventoryManager
    } catch (e: Exception) {
        null
    } ?: return

    val groups = inventoryManager.getInventoryGroups()
    val allItems = groups.flatMap { it.items }

    if (allItems.isEmpty()) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("No Items Yet")
            .setMessage("Add some items to your inventory first!")
            .setPositiveButton("OK", null)
            .show()
        return
    }

    // Show dialog
    val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
        .setTitle("🤖 AI Item Descriptions")
        .setMessage("Generating using ${ItemDescriptionGenerator.getModelInfo()}...")
        .setPositiveButton("Close", null)
        .create()

    dialog.show()

    // Generate descriptions
    CoroutineScope(Dispatchers.Main).launch {
        val descriptions = mutableListOf<Pair<String, String>>()

        withContext(Dispatchers.IO) {
            allItems.take(5).forEach { item ->
                val desc = ItemDescriptionGenerator.generateDescription(item.className)
                descriptions.add(item.displayName to desc)
            }
        }

        val formattedText = descriptions.joinToString("\n\n") { (name, desc) ->
            "📦 $name:\n$desc"
        }

        dialog.setMessage(formattedText)
    }
}

/**
 * Handle highlighted region with AI vision identification (ExecuTorch + Vision LLM)
 */
suspend fun MainActivity.identifyHighlightedItem(bitmap: Bitmap, detector: ObjectDetector?): String {
    // Use vision-language model to identify the item
    return ItemDescriptionGenerator.identifyFromImage(bitmap, detector)
}

/**
 * Crop bitmap to the bounds of the drawn path
 */
fun MainActivity.cropBitmapToPath(bitmap: Bitmap, path: android.graphics.Path): Bitmap {
    // Get the bounding rectangle of the path
    val bounds = android.graphics.RectF()
    path.computeBounds(bounds, true)

    // Ensure bounds are within bitmap dimensions
    val left = bounds.left.toInt().coerceIn(0, bitmap.width - 1)
    val top = bounds.top.toInt().coerceIn(0, bitmap.height - 1)
    val right = bounds.right.toInt().coerceIn(left + 1, bitmap.width)
    val bottom = bounds.bottom.toInt().coerceIn(top + 1, bitmap.height)

    val width = (right - left).coerceAtLeast(1)
    val height = (bottom - top).coerceAtLeast(1)

    // Crop the bitmap
    return try {
        Bitmap.createBitmap(bitmap, left, top, width, height)
    } catch (e: Exception) {
        android.util.Log.e("MainActivity", "Error cropping bitmap", e)
        bitmap // Return original if cropping fails
    }
}

/**
 * Setup AI vision-based highlight feature (ExecuTorch + Vision LLM)
 */
fun MainActivity.setupAIVisionHighlight() {
    android.util.Log.d("ExecuTorch", "setupAIVisionHighlight called - scheduling delayed setup")

    // Post delayed to ensure drawingOverlay is initialized
    window.decorView.post {
        android.util.Log.d("ExecuTorch", "Delayed setup executing")

        // Get the drawing overlay
        val drawingOverlay = try {
            val field = this::class.java.getDeclaredField("drawingOverlay")
            field.isAccessible = true
            val overlay = field.get(this) as? DrawingOverlayView
            android.util.Log.d("ExecuTorch", "DrawingOverlay retrieved: ${overlay != null}")
            overlay
        } catch (e: Exception) {
            android.util.Log.e("ExecuTorch", "Error getting drawingOverlay", e)
            null
        } ?: run {
            android.util.Log.e("ExecuTorch", "DrawingOverlay is null, cannot setup AI vision")
            return@post
        }

        android.util.Log.d("ExecuTorch", "Setting drawing complete listener for AI vision")
        // Override the drawing complete listener to use AI vision
        drawingOverlay.setOnDrawingCompleteListener { path ->
            android.util.Log.d("ExecuTorch", "Drawing complete callback triggered!")
            handleAIVisionHighlight(path)
        }
        android.util.Log.d("ExecuTorch", "AI vision setup complete")
    }
}

/**
 * Handle highlighted region with AI vision identification
 */
private fun MainActivity.handleAIVisionHighlight(path: android.graphics.Path) {
    android.util.Log.d("ExecuTorch", "handleAIVisionHighlight called")

    // Get current camera frame
    val bitmap = getCurrentFrame() ?: run {
        android.util.Log.w("ExecuTorch", "No camera frame available, falling back to manual entry")
        // Fallback to manual entry if no frame available
        showAddItemDialog()
        return
    }

    // Crop bitmap to the highlighted region
    val croppedBitmap = cropBitmapToPath(bitmap, path)
    android.util.Log.d("ExecuTorch", "Cropped bitmap to ${croppedBitmap.width}x${croppedBitmap.height}")

    android.util.Log.d("ExecuTorch", "Got camera frame, showing loading dialog")
    // Show loading dialog
    val loadingDialog = androidx.appcompat.app.AlertDialog.Builder(this)
        .setTitle("🤖 AI Vision Analyzing...")
        .setMessage("Using object detection on Qualcomm NPU to identify the highlighted item...")
        .setCancelable(false)
        .create()

    loadingDialog.show()

    // Get the detector
    val detector = try {
        val field = this::class.java.getDeclaredField("detector")
        field.isAccessible = true
        field.get(this) as? ObjectDetector
    } catch (e: Exception) {
        android.util.Log.e("ExecuTorch", "Error getting detector", e)
        null
    }

    // Run AI vision identification on the cropped region
    CoroutineScope(Dispatchers.Main).launch {
        try {
            android.util.Log.d("ExecuTorch", "Starting AI identification on cropped region...")
            val itemName = withContext(Dispatchers.IO) {
                ItemDescriptionGenerator.identifyFromImage(croppedBitmap, detector)
            }

            android.util.Log.d("ExecuTorch", "AI identified: $itemName")
            loadingDialog.dismiss()

            // Show result and add item (use original bitmap for thumbnail)
            showAIIdentifiedItemDialog(itemName, bitmap, path)
        } catch (e: Exception) {
            android.util.Log.e("ExecuTorch", "Error during AI identification", e)
            loadingDialog.dismiss()
            // Fallback to manual entry
            showAddItemDialog()
        }
    }
}

/**
 * Show dialog for AI-identified item
 */
private fun MainActivity.showAIIdentifiedItemDialog(itemName: String, bitmap: Bitmap, path: android.graphics.Path) {
    androidx.appcompat.app.AlertDialog.Builder(this)
        .setTitle("✨ AI Identified Item")
        .setMessage("Object detection (ExecuTorch on Qualcomm NPU) identified:\n\n\"$itemName\"\n\nAdd to inventory?")
        .setPositiveButton("Add") { _, _ ->
            // Add the item with AI-identified name
            addManualItem(itemName, bitmap, path)
            clearDrawing()
        }
        .setNegativeButton("Cancel") { _, _ ->
            clearDrawing()
        }
        .setNeutralButton("Edit Name") { _, _ ->
            // Allow editing the name
            showAddItemDialog()
        }
        .show()
}

/**
 * Helper to get current camera frame
 */
private fun MainActivity.getCurrentFrame(): Bitmap? {
    return try {
        val previewView = findViewById<androidx.camera.view.PreviewView>(R.id.previewView)
        previewView.bitmap
    } catch (e: Exception) {
        null
    }
}

/**
 * Helper to add manual item
 */
private fun MainActivity.addManualItem(itemName: String, bitmap: Bitmap?, path: android.graphics.Path) {
    try {
        val inventoryManager = this::class.java.getDeclaredField("inventoryManager").apply {
            isAccessible = true
        }.get(this) as? InventoryManager

        val photoManager = this::class.java.getDeclaredField("photoManager").apply {
            isAccessible = true
        }.get(this) as? PhotoManager

        // Generate unique ID for this item
        val itemId = java.util.UUID.randomUUID().toString()

        // Get the drawing overlay view to get its dimensions
        val drawingOverlay = try {
            val field = this::class.java.getDeclaredField("drawingOverlay")
            field.isAccessible = true
            field.get(this) as? DrawingOverlayView
        } catch (e: Exception) {
            null
        }

        // Calculate the bounding box in screen coordinates
        val screenBounds = android.graphics.RectF()
        path.computeBounds(screenBounds, true)

        // Scale screen coordinates to bitmap coordinates
        val bbox = if (bitmap != null && drawingOverlay != null) {
            val previewView = findViewById<androidx.camera.view.PreviewView>(R.id.previewView)

            // Get the scale factors between preview view and actual bitmap
            val scaleX = bitmap.width.toFloat() / previewView.width.toFloat()
            val scaleY = bitmap.height.toFloat() / previewView.height.toFloat()

            BoundingBox(
                x1 = screenBounds.left * scaleX,
                y1 = screenBounds.top * scaleY,
                x2 = screenBounds.right * scaleX,
                y2 = screenBounds.bottom * scaleY
            )
        } else {
            BoundingBox(
                x1 = screenBounds.left,
                y1 = screenBounds.top,
                x2 = screenBounds.right,
                y2 = screenBounds.bottom
            )
        }

        android.util.Log.d("ExecuTorch", "Saving photo with bbox: $bbox")

        // Save photo with scaled bounding box
        val photoPath = if (bitmap != null && photoManager != null) {
            photoManager.saveItemPhoto(itemId, bitmap, bbox)
        } else {
            null
        }

        android.util.Log.d("ExecuTorch", "Photo saved to: $photoPath")

        val trackedItem = TrackedItem(
            id = itemId,
            className = itemName,
            pricePerItem = CocoClasses.getPrice(itemName),
            photoPath = photoPath,
            boundingBox = bbox
        )

        inventoryManager?.addManualItem(trackedItem)

        // Refresh UI
        runOnUiThread {
            updateInventoryDisplay()
            android.widget.Toast.makeText(
                this,
                "✨ Added \"$itemName\" (AI Vision) - $${trackedItem.pricePerItem.toInt()}",
                android.widget.Toast.LENGTH_LONG
            ).show()
        }
    } catch (e: Exception) {
        android.util.Log.e("ExecuTorch", "Error adding manual item", e)
        e.printStackTrace()
    }
}

/**
 * Helper to call updateInventoryDisplay via reflection
 */
private fun MainActivity.updateInventoryDisplay() {
    try {
        val method = this::class.java.getDeclaredMethod("updateInventoryDisplay")
        method.isAccessible = true
        method.invoke(this)
    } catch (e: Exception) {
        android.util.Log.e("ExecuTorch", "Error updating inventory display", e)
        e.printStackTrace()
    }
}

/**
 * Helper to clear drawing
 */
private fun MainActivity.clearDrawing() {
    try {
        val drawingOverlay = this::class.java.getDeclaredField("drawingOverlay").apply {
            isAccessible = true
        }.get(this) as? DrawingOverlayView

        drawingOverlay?.clearDrawing()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

/**
 * Helper to show manual add dialog (fallback)
 */
private fun MainActivity.showAddItemDialog() {
    // Call the original method via reflection
    try {
        val method = this::class.java.getDeclaredMethod("showAddItemDialog")
        method.isAccessible = true
        method.invoke(this)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
