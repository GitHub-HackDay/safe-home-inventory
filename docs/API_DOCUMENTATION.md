# API Documentation - SafeHome Inventory

## Overview

This document provides comprehensive API documentation for the SafeHome Inventory Android application, detailing the internal architecture, public interfaces, and integration points for developers.

## Core Architecture

### Component Overview
```
┌─────────────────────────────────────────────────────────────┐
│                      MainActivity                           │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐
│  │   CameraX API   │  │  YoloV8Detector │  │ InventoryManager│
│  └─────────────────┘  └─────────────────┘  └─────────────────┘
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐
│  │   OverlayView   │  │ InventoryAdapter│  │  CocoClasses    │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘
└─────────────────────────────────────────────────────────────┘
```

## Public APIs

### YoloV8Detector

#### Class Declaration
```kotlin
class YoloV8Detector(context: Context) {
    fun detect(bitmap: Bitmap): List<Detection>
    fun close()
}
```

#### Constructor
```kotlin
YoloV8Detector(context: Context)
```
- **Parameters**: 
  - `context`: Android application context for asset access
- **Purpose**: Initializes ONNX Runtime session and loads YOLOv8 model
- **Throws**: Exception if model loading fails

#### detect() Method
```kotlin
fun detect(bitmap: Bitmap): List<Detection>
```
- **Parameters**:
  - `bitmap`: Input image for object detection (any size)
- **Returns**: List of detected objects with bounding boxes and confidence
- **Performance**: ~170ms on Samsung Galaxy S25
- **Thread Safety**: Not thread-safe, use single inference thread

**Usage Example**:
```kotlin
val detector = YoloV8Detector(context)
val bitmap = // ... obtain bitmap from camera
val detections = detector.detect(bitmap)

detections.forEach { detection ->
    println("Found ${detection.className} with ${detection.confidence} confidence")
}
```

#### close() Method
```kotlin
fun close()
```
- **Purpose**: Releases ONNX Runtime resources
- **When to Call**: In onDestroy() or when detector no longer needed
- **Important**: Must call to prevent memory leaks

### InventoryManager

#### Class Declaration
```kotlin
class InventoryManager {
    fun addDetections(detections: List<Detection>): Boolean
    fun getInventoryGroups(): List<InventoryItemGroup>
    fun toggleGroupExpansion(className: String)
    fun updateItemName(itemId: String, newName: String)
    fun removeItem(itemId: String): Boolean
    fun getTotalValue(): Double
    fun clear()
}
```

#### addDetections() Method
```kotlin
fun addDetections(detections: List<Detection>): Boolean
```
- **Parameters**:
  - `detections`: List of detection results from YoloV8Detector
- **Returns**: `true` if any new items were added, `false` if all filtered by cooldown
- **Behavior**: Applies 3-second cooldown per object class to prevent duplicates

**Usage Example**:
```kotlin
val manager = InventoryManager()
val detections = detector.detect(bitmap)
val updated = manager.addDetections(detections)

if (updated) {
    // Update UI to reflect new inventory items
    updateInventoryDisplay()
}
```

#### getInventoryGroups() Method
```kotlin
fun getInventoryGroups(): List<InventoryItemGroup>
```
- **Returns**: Grouped inventory items sorted by total value (descending)
- **Purpose**: Provides data structure for RecyclerView display
- **Performance**: O(n log n) due to sorting

#### Item Management Methods
```kotlin
// Toggle group expansion state
fun toggleGroupExpansion(className: String)

// Update custom name for individual item
fun updateItemName(itemId: String, newName: String)

// Remove specific item from inventory
fun removeItem(itemId: String): Boolean

// Get total monetary value of all items
fun getTotalValue(): Double

// Clear all inventory data
fun clear()
```

### InventoryAdapter

#### Class Declaration
```kotlin
class InventoryAdapter(
    private val onGroupClick: (String) -> Unit,
    private val onItemNameEdit: (String, String) -> Unit,
    private val onItemDelete: (String) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>()
```

#### Constructor Parameters
- **onGroupClick**: Callback when group header is tapped
- **onItemNameEdit**: Callback when item name is edited
- **onItemDelete**: Callback when item is deleted

#### updateGroups() Method
```kotlin
fun updateGroups(newGroups: List<InventoryItemGroup>)
```
- **Parameters**: New inventory groups to display
- **Behavior**: Updates RecyclerView with new data
- **UI Update**: Calls `notifyDataSetChanged()` for immediate refresh

**Usage Example**:
```kotlin
val adapter = InventoryAdapter(
    onGroupClick = { className -> 
        inventoryManager.toggleGroupExpansion(className)
        updateInventoryDisplay()
    },
    onItemNameEdit = { itemId, newName ->
        inventoryManager.updateItemName(itemId, newName)
        updateInventoryDisplay()
    },
    onItemDelete = { itemId ->
        inventoryManager.removeItem(itemId)
        updateInventoryDisplay()
    }
)

recyclerView.adapter = adapter
```

### OverlayView

#### Class Declaration
```kotlin
class OverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs)
```

#### updateDetections() Method
```kotlin
fun updateDetections(detections: List<Detection>)
```
- **Parameters**: List of current detection results
- **Behavior**: Triggers view redraw with new bounding boxes
- **Performance**: Lightweight, only invalidates view

**Usage Example**:
```kotlin
val overlayView = findViewById<OverlayView>(R.id.overlayView)
val detections = detector.detect(bitmap)
overlayView.updateDetections(detections)
```

## Data Models

### Detection
```kotlin
data class Detection(
    val classIndex: Int,        // COCO class index (0-79)
    val className: String,      // Human-readable name
    val confidence: Float,      // Detection confidence (0.0-1.0)
    val bbox: BoundingBox      // Normalized bounding box
)
```

**Field Details**:
- **classIndex**: Maps to CocoClasses.LABELS array
- **className**: Direct lookup from LABELS[classIndex]
- **confidence**: YOLOv8 output confidence score
- **bbox**: Coordinates normalized to [0.0, 1.0] range

### BoundingBox
```kotlin
data class BoundingBox(
    val x1: Float,  // Left edge (normalized)
    val y1: Float,  // Top edge (normalized)
    val x2: Float,  // Right edge (normalized)
    val y2: Float   // Bottom edge (normalized)
)
```

**Coordinate System**:
- **Origin**: Top-left corner (0.0, 0.0)
- **Range**: [0.0, 1.0] for both x and y axes
- **Conversion**: Multiply by view width/height for screen coordinates

### TrackedItem
```kotlin
data class TrackedItem(
    val id: String = UUID.randomUUID().toString(),
    val className: String,
    var customName: String? = null,
    val detectedAt: Long = System.currentTimeMillis(),
    val pricePerItem: Double,
    var notes: String? = null
) {
    val displayName: String get() = customName ?: className
}
```

**Field Descriptions**:
- **id**: Unique identifier for item management
- **className**: Original detected class name
- **customName**: User-defined name (nullable)
- **detectedAt**: Timestamp of first detection
- **pricePerItem**: Estimated monetary value
- **displayName**: Computed property returning custom or class name

### InventoryItemGroup
```kotlin
data class InventoryItemGroup(
    val className: String,
    val items: List<TrackedItem>,
    val isExpanded: Boolean = false
) {
    val count: Int get() = items.size
    val totalValue: Double get() = items.sumOf { it.pricePerItem }
    val pricePerItem: Double get() = items.firstOrNull()?.pricePerItem ?: 0.0
}
```

**Computed Properties**:
- **count**: Number of items in group
- **totalValue**: Sum of all item values in group
- **pricePerItem**: Price of first item (assumes uniform pricing)

## Constants and Configuration

### CocoClasses
```kotlin
object CocoClasses {
    val LABELS: Array<String>  // 80 COCO class names
    fun getPrice(className: String): Double
}
```

#### Supported Classes
```kotlin
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
```

#### Price Mapping
```kotlin
private val PRICES = mapOf(
    "tv" to 500.0,
    "laptop" to 1200.0,
    "couch" to 800.0,
    "bed" to 600.0,
    "refrigerator" to 1500.0,
    "microwave" to 150.0,
    "oven" to 800.0,
    "chair" to 100.0,
    "dining table" to 400.0,
    "book" to 20.0,
    "clock" to 50.0,
    "cell phone" to 700.0,
    "bicycle" to 400.0
)

fun getPrice(className: String): Double = PRICES[className] ?: 100.0
```

### Detection Parameters
```kotlin
// YoloV8Detector constants
private val confThreshold = 0.5f    // Confidence threshold
private val iouThreshold = 0.45f    // IoU threshold for NMS
private val inputSize = 640         // Model input size (640x640)

// InventoryManager constants
private val cooldownMs = 3000L      // 3-second deduplication cooldown
```

## Integration Examples

### Basic Detection Pipeline
```kotlin
class MainActivity : AppCompatActivity() {
    private lateinit var detector: YoloV8Detector
    private val inventoryManager = InventoryManager()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize detector
        detector = YoloV8Detector(this)
        
        // Set up camera processing
        setupCameraAnalysis()
    }
    
    private fun processImage(imageProxy: ImageProxy) {
        // Convert to bitmap
        val bitmap = imageProxy.toBitmap()
        val rotated = rotateBitmap(bitmap, imageProxy.imageInfo.rotationDegrees.toFloat())
        
        // Run detection
        val detections = detector.detect(rotated)
        
        // Update inventory
        val updated = inventoryManager.addDetections(detections)
        
        // Update UI if needed
        if (updated) {
            runOnUiThread { updateInventoryDisplay() }
        }
        
        imageProxy.close()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        detector.close()
    }
}
```

### Custom Detection Filtering
```kotlin
class CustomInventoryManager : InventoryManager() {
    // Filter detections by confidence threshold
    fun addHighConfidenceDetections(detections: List<Detection>): Boolean {
        val filtered = detections.filter { it.confidence > 0.7f }
        return super.addDetections(filtered)
    }
    
    // Filter by specific object classes
    fun addElectronicsOnly(detections: List<Detection>): Boolean {
        val electronics = setOf("tv", "laptop", "cell phone", "keyboard", "mouse")
        val filtered = detections.filter { it.className in electronics }
        return super.addDetections(filtered)
    }
}
```

### Custom Price Estimation
```kotlin
object CustomPricing {
    private val customPrices = mapOf(
        "laptop" to 1500.0,     // Higher estimate for laptops
        "tv" to 800.0,          // Updated TV prices
        "cell phone" to 900.0   // Current phone prices
    )
    
    fun getCustomPrice(className: String): Double {
        return customPrices[className] ?: CocoClasses.getPrice(className)
    }
}

// Usage in TrackedItem creation
TrackedItem(
    className = detection.className,
    pricePerItem = CustomPricing.getCustomPrice(detection.className)
)
```

## Error Handling

### Exception Types
```kotlin
// Model loading errors
try {
    val detector = YoloV8Detector(context)
} catch (e: Exception) {
    Log.e("SafeHome", "Failed to load YOLOv8 model", e)
    // Fallback: disable detection features
}

// Inference errors
try {
    val detections = detector.detect(bitmap)
} catch (e: Exception) {
    Log.e("SafeHome", "Inference failed", e)
    // Return empty detection list
    return emptyList()
}

// Camera permission errors
if (!hasCameraPermission()) {
    requestCameraPermission()
    return
}
```

### Best Practices
```kotlin
// Always close detector in onDestroy
override fun onDestroy() {
    super.onDestroy()
    detector.close()  // Prevents memory leaks
}

// Handle null/empty detection results
val detections = detector.detect(bitmap)
if (detections.isNotEmpty()) {
    inventoryManager.addDetections(detections)
}

// Validate bitmap before inference
if (bitmap.isRecycled) {
    Log.w("SafeHome", "Bitmap is recycled, skipping detection")
    return emptyList()
}
```

## Performance Considerations

### Memory Management
```kotlin
// Efficient bitmap handling
fun processImage(imageProxy: ImageProxy) {
    val bitmap = imageProxy.toBitmap()
    try {
        val detections = detector.detect(bitmap)
        // Process detections...
    } finally {
        // Bitmap automatically garbage collected
        imageProxy.close()  // Release camera buffer
    }
}
```

### Threading
```kotlin
// UI thread management
runOnUiThread {
    overlayView.updateDetections(detections)
    updateInventoryDisplay()
}

// Background processing
val cameraExecutor = Executors.newSingleThreadExecutor()
imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
    processImage(imageProxy)  // Runs on background thread
}
```

### Optimization Tips
1. **Single Inference Thread**: Use one thread for all ML operations
2. **UI Updates**: Only update UI when inventory actually changes
3. **Memory Cleanup**: Close detector and clear large objects
4. **Batch Processing**: Process multiple detections together when possible

## Testing and Validation

### Unit Testing APIs
```kotlin
@Test
fun testDetectionFiltering() {
    val manager = InventoryManager()
    val highConfDetection = Detection(0, "person", 0.8f, BoundingBox(0f, 0f, 1f, 1f))
    val lowConfDetection = Detection(1, "bicycle", 0.3f, BoundingBox(0f, 0f, 1f, 1f))
    
    val detections = listOf(highConfDetection, lowConfDetection)
    val updated = manager.addDetections(detections)
    
    assertTrue(updated)
    assertEquals(1, manager.getInventoryGroups().size)  // Only high-conf added
}
```

### Performance Testing
```kotlin
@Test
fun testInferencePerformance() {
    val detector = YoloV8Detector(context)
    val bitmap = createTestBitmap(640, 640)
    
    val startTime = System.currentTimeMillis()
    repeat(10) {
        detector.detect(bitmap)
    }
    val avgTime = (System.currentTimeMillis() - startTime) / 10
    
    assertTrue("Inference too slow: ${avgTime}ms", avgTime < 200)
}
```

## Migration and Updates

### Version Compatibility
- **Current Version**: 1.0 (ONNX Runtime)
- **Future Version**: 2.0 (ExecuTorch)
- **API Stability**: Public interfaces will remain backward compatible

### Migration Path
```kotlin
// Future ExecuTorch integration
class YoloV8ExecutorchDetector(context: Context) : YoloV8Detector(context) {
    // Same public API, different implementation
    override fun detect(bitmap: Bitmap): List<Detection> {
        // ExecuTorch inference implementation
    }
}
```

---

**API Documentation v1.0 - SafeHome Inventory**

*Complete developer reference for integrating and extending SafeHome Inventory functionality*