package com.safehome.inventory

object CocoClasses {
    val LABELS = arrayOf(
        "person", "bicycle", "car", "motorcycle", "airplane", "bus", "train", "truck", "boat",
        "traffic light", "fire hydrant", "stop sign", "parking meter", "bench", "bird", "cat",
        "dog", "horse", "sheep", "cow", "elephant", "bear", "zebra", "giraffe", "backpack",
        "umbrella", "handbag", "tie", "suitcase", "frisbee", "skis", "snowboard", "sports ball",
        "kite", "baseball bat", "baseball glove", "skateboard", "surfboard", "tennis racket",
        "bottle", "wine glass", "cup", "fork", "knife", "spoon", "bowl", "banana", "apple",
        "sandwich", "orange", "broccoli", "carrot", "hot dog", "pizza", "donut", "cake", "chair",
        "couch", "potted plant", "bed", "dining table", "toilet", "tv", "laptop", "mouse",
        "remote", "keyboard", "cell phone", "microwave", "oven", "toaster", "sink",
        "refrigerator", "book", "clock", "vase", "scissors", "teddy bear", "hair drier",
        "toothbrush"
    )

    // Items that should NOT be tracked (people, animals, food, perishables)
    private val EXCLUDED_ITEMS = setOf(
        // People
        "person",
        // Animals
        "bird", "cat", "dog", "horse", "sheep", "cow", "elephant", "bear", "zebra", "giraffe",
        // Food/Perishables (not property)
        "banana", "apple", "sandwich", "orange", "broccoli", "carrot", "hot dog", "pizza",
        "donut", "cake", "bottle", "wine glass", "cup",
        // Outdoor/Street items (not home property)
        "traffic light", "fire hydrant", "stop sign", "parking meter", "bench"
    )

    private val PRICES = mapOf(
        "tv" to 500.0, "laptop" to 1200.0, "couch" to 800.0, "bed" to 600.0,
        "refrigerator" to 1500.0, "microwave" to 150.0, "oven" to 800.0,
        "chair" to 100.0, "dining table" to 400.0, "book" to 20.0,
        "clock" to 50.0, "cell phone" to 700.0, "bicycle" to 400.0,
        "backpack" to 60.0, "umbrella" to 30.0, "handbag" to 150.0,
        "suitcase" to 100.0, "keyboard" to 100.0, "mouse" to 40.0,
        "remote" to 20.0, "vase" to 50.0, "scissors" to 15.0,
        "hair drier" to 40.0, "toaster" to 50.0, "potted plant" to 30.0
    )

    fun getPrice(className: String): Double {
        // Manual items already have their price set, just return default for grouping
        if (className == "manual") return 0.0
        return PRICES[className] ?: 100.0
    }

    fun isPropertyItem(className: String): Boolean {
        // Always allow manual items
        if (className == "manual") return true
        return !EXCLUDED_ITEMS.contains(className)
    }
}
