package com.safehome.inventory

import android.graphics.Bitmap

class InventoryManager {
    private val trackedItems = mutableListOf<TrackedItem>()
    private val lastDetectionTime = mutableMapOf<String, Long>()
    private val ignoredClasses = mutableSetOf<String>()  // Classes to never track
    private val cooldownMs = 3000L
    private val expandedGroups = mutableSetOf<String>()

    var photoManager: PhotoManager? = null

    fun addDetections(detections: List<Detection>, capturedFrame: Bitmap? = null): Boolean {
        var updated = false
        val currentTime = System.currentTimeMillis()

        for (detection in detections) {
            val className = detection.className

            // Skip non-property items (people, animals, food)
            if (!CocoClasses.isPropertyItem(className)) {
                continue
            }

            // Skip ignored classes
            if (ignoredClasses.contains(className)) {
                continue
            }

            val lastTime = lastDetectionTime[className] ?: 0L

            if (currentTime - lastTime > cooldownMs) {
                // Capture photo if frame available
                val photoPath = if (capturedFrame != null && photoManager != null) {
                    val itemId = java.util.UUID.randomUUID().toString()
                    photoManager?.saveItemPhoto(itemId, capturedFrame, detection.bbox)
                } else {
                    null
                }

                // Add new tracked item
                trackedItems.add(
                    TrackedItem(
                        className = className,
                        pricePerItem = CocoClasses.getPrice(className),
                        photoPath = photoPath,
                        boundingBox = detection.bbox
                    )
                )

                lastDetectionTime[className] = currentTime
                updated = true
            }
        }

        return updated
    }

    fun ignoreClass(className: String) {
        ignoredClasses.add(className)
        // Remove all items of this class
        trackedItems.removeAll { it.className == className }
    }

    fun unignoreClass(className: String) {
        ignoredClasses.remove(className)
    }

    fun getInventoryGroups(): List<InventoryItemGroup> {
        return trackedItems
            .groupBy { it.className }
            .map { (className, items) ->
                InventoryItemGroup(
                    className = className,
                    items = items.sortedByDescending { it.detectedAt },
                    // Auto-expand manual items by default
                    isExpanded = expandedGroups.contains(className) || className == "manual"
                )
            }
            .sortedByDescending { it.totalValue }
    }

    fun toggleGroupExpansion(className: String) {
        if (expandedGroups.contains(className)) {
            expandedGroups.remove(className)
        } else {
            expandedGroups.add(className)
        }
    }

    fun updateItemName(itemId: String, newName: String) {
        trackedItems.find { it.id == itemId }?.customName = newName
    }

    fun updateItemPrice(itemId: String, newPrice: Double) {
        trackedItems.find { it.id == itemId }?.let {
            trackedItems[trackedItems.indexOf(it)] = it.copy(pricePerItem = newPrice)
        }
    }

    fun removeItem(itemId: String): Boolean {
        return trackedItems.removeIf { it.id == itemId }
    }

    fun getTotalValue(): Double = trackedItems.sumOf { it.pricePerItem }

    fun clear() {
        trackedItems.clear()
        lastDetectionTime.clear()
        expandedGroups.clear()
        ignoredClasses.clear()
    }

    fun clearIgnoredItems() {
        ignoredClasses.clear()
    }

    fun addManualItem(item: TrackedItem) {
        trackedItems.add(item)
    }
}
