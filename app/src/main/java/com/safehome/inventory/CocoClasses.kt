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

    private val PRICES = mapOf(
        "tv" to 500.0, "laptop" to 1200.0, "couch" to 800.0, "bed" to 600.0,
        "refrigerator" to 1500.0, "microwave" to 150.0, "oven" to 800.0,
        "chair" to 100.0, "dining table" to 400.0, "book" to 20.0,
        "clock" to 50.0, "cell phone" to 700.0, "bicycle" to 400.0
    )

    fun getPrice(className: String): Double = PRICES[className] ?: 100.0
}
