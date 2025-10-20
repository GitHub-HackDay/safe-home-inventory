package com.safehome.inventory

class InventoryManager {
    private val trackedItems = mutableListOf<TrackedItem>()
    private val lastDetectionTime = mutableMapOf<String, Long>()
    private val cooldownMs = 3000L
    private val expandedGroups = mutableSetOf<String>()

    fun addDetections(detections: List<Detection>): Boolean {
        var updated = false
        val currentTime = System.currentTimeMillis()

        for (detection in detections) {
            val className = detection.className
            val lastTime = lastDetectionTime[className] ?: 0L

            if (currentTime - lastTime > cooldownMs) {
                // Add new tracked item
                trackedItems.add(
                    TrackedItem(
                        className = className,
                        pricePerItem = CocoClasses.getPrice(className)
                    )
                )

                lastDetectionTime[className] = currentTime
                updated = true
            }
        }

        return updated
    }

    fun getInventoryGroups(): List<InventoryItemGroup> {
        return trackedItems
            .groupBy { it.className }
            .map { (className, items) ->
                InventoryItemGroup(
                    className = className,
                    items = items.sortedByDescending { it.detectedAt },
                    isExpanded = expandedGroups.contains(className)
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
    }
}
