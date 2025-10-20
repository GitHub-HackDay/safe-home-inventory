package com.safehome.inventory

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
