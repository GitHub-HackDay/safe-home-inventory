package com.safehome.inventory

import java.util.UUID

data class Detection(
    val classIndex: Int,
    val className: String,
    val confidence: Float,
    val bbox: BoundingBox
)

data class BoundingBox(
    val x1: Float,
    val y1: Float,
    val x2: Float,
    val y2: Float
)

// Individual tracked item with unique ID
data class TrackedItem(
    val id: String = UUID.randomUUID().toString(),
    val className: String,
    var customName: String? = null,  // User can set custom name
    val detectedAt: Long = System.currentTimeMillis(),
    val pricePerItem: Double,
    var notes: String? = null
) {
    val displayName: String get() = customName ?: className
}

// Grouped inventory view (for display)
data class InventoryItemGroup(
    val className: String,
    val items: List<TrackedItem>,
    val isExpanded: Boolean = false
) {
    val count: Int get() = items.size
    val totalValue: Double get() = items.sumOf { it.pricePerItem }
    val pricePerItem: Double get() = items.firstOrNull()?.pricePerItem ?: 0.0
}
