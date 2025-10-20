# SafeHome Inventory - Technical Specification

## Overview

SafeHome Inventory is a privacy-first mobile Android application that uses on-device AI object detection to catalog home inventory items. Built for the ExecuTorch Hackathon at GitHub HQ (October 20, 2025), the app leverages YOLOv8 neural networks running locally on device with ONNX Runtime.

## Architecture

### High-Level Architecture

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   Camera Feed   │───▶│   YOLOv8 Model   │───▶│ Inventory Store │
│  (CameraX API)  │    │ (ONNX Runtime)   │    │ (In-Memory)     │
└─────────────────┘    └──────────────────┘    └─────────────────┘
         │                        │                       │
         ▼                        ▼                       ▼
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│  Preview View   │    │  Overlay View    │    │ RecyclerView UI │
│                 │    │  (Bounding Box)  │    │                 │
└─────────────────┘    └──────────────────┘    └─────────────────┘
```

### Core Components

#### 1. MainActivity
- **Purpose**: Main application controller and UI coordinator
- **Key Responsibilities**:
  - Camera permission management
  - CameraX lifecycle management
  - Image processing pipeline coordination
  - UI component orchestration
- **Dependencies**: CameraX, RecyclerView, Material Design components

#### 2. YoloV8Detector
- **Purpose**: On-device AI inference engine
- **Key Responsibilities**:
  - ONNX model loading and initialization
  - Image preprocessing (640x640 normalization)
  - YOLOv8 inference execution
  - Non-Maximum Suppression (NMS) filtering
  - Detection result parsing
- **Performance**: ~170ms inference time on target hardware
- **Model**: YOLOv8n (Nano) - 12.2MB ONNX FP32 format

#### 3. InventoryManager
- **Purpose**: Business logic and data management
- **Key Responsibilities**:
  - Item deduplication (3-second cooldown)
  - Inventory grouping and organization
  - Value calculation and aggregation
  - CRUD operations for tracked items
- **Data Structures**: 
  - `TrackedItem`: Individual inventory items with UUID
  - `InventoryItemGroup`: Grouped display collections

#### 4. InventoryAdapter
- **Purpose**: RecyclerView adapter for inventory display
- **Key Responsibilities**:
  - Dynamic list management (expandable groups)
  - Item editing and deletion interfaces
  - Real-time UI updates
- **UI Pattern**: Expandable group headers with individual item details

#### 5. OverlayView
- **Purpose**: Real-time detection visualization
- **Key Responsibilities**:
  - Bounding box rendering
  - Confidence score display
  - Coordinate transformation (model space → screen space)

## Technical Stack

### Core Technologies
- **Language**: Kotlin (100%)
- **Minimum SDK**: API 30 (Android 11)
- **Target SDK**: API 34 (Android 14)
- **Build System**: Gradle 8.7+ with Kotlin DSL

### Key Dependencies
- **ML Runtime**: ONNXRuntime Mobile 1.18.0
- **Camera**: CameraX 1.3.1 (Core, Camera2, Lifecycle, View)
- **UI Framework**: Material Design 3
- **RecyclerView**: AndroidX RecyclerView 1.3.2

### AI/ML Components
- **Model**: YOLOv8n (Ultralytics)
- **Format**: ONNX FP32
- **Input Size**: 640x640x3 (RGB)
- **Output**: [1, 84, 8400] tensor
- **Classes**: 80 COCO object classes
- **Inference Backend**: ONNX Runtime CPU backend

## Data Models

### Detection
```kotlin
data class Detection(
    val classIndex: Int,        // COCO class index (0-79)
    val className: String,      // Human-readable class name
    val confidence: Float,      // Detection confidence (0.0-1.0)
    val bbox: BoundingBox      // Normalized bounding box
)
```

### TrackedItem
```kotlin
data class TrackedItem(
    val id: String,             // UUID for unique identification
    val className: String,     // Original detected class
    var customName: String?,   // User-defined custom name
    val detectedAt: Long,      // Detection timestamp
    val pricePerItem: Double,  // Estimated monetary value
    var notes: String?         // Optional user notes
)
```

### InventoryItemGroup
```kotlin
data class InventoryItemGroup(
    val className: String,     // Group identifier
    val items: List<TrackedItem>, // Grouped items
    val isExpanded: Boolean    // UI expansion state
)
```

## Algorithms and Processing

### Object Detection Pipeline

1. **Image Acquisition**: CameraX ImageAnalysis callback
2. **Preprocessing**: 
   - Bitmap conversion from ImageProxy
   - Rotation correction based on device orientation
   - Resize to 640x640 maintaining aspect ratio
   - RGB normalization (0-255 → 0.0-1.0)
   - Channel reordering (HWC → CHW)

3. **Inference**:
   - ONNX tensor creation
   - YOLOv8 forward pass
   - Output tensor extraction

4. **Post-processing**:
   - Confidence thresholding (threshold: 0.5)
   - Non-Maximum Suppression (IoU threshold: 0.45)
   - Coordinate denormalization

### Deduplication Strategy
- **Cooldown Period**: 3 seconds per object class
- **Logic**: Prevents duplicate entries of same class within cooldown window
- **Implementation**: `HashMap<String, Long>` tracking last detection time

### Value Estimation
- **Static Pricing**: Hardcoded price map for common items
- **Default Price**: $100 for unrecognized items
- **Aggregation**: Real-time total value calculation across all items

## Performance Characteristics

### Inference Performance
- **Target Device**: Samsung Galaxy S25 (Snapdragon 8 Elite)
- **Inference Time**: ~170ms per frame
- **Memory Usage**: ~12.2MB for model + runtime overhead
- **CPU Utilization**: Single-threaded inference on CPU backend

### UI Performance
- **Frame Rate**: Real-time camera preview (30 FPS)
- **Overlay Updates**: Synchronized with detection results
- **List Updates**: Optimized with RecyclerView.notifyDataSetChanged()

## Privacy and Security

### On-Device Processing
- **No Network Calls**: 100% local processing
- **No Cloud Storage**: All data remains on device
- **No Telemetry**: No usage analytics or data collection

### Data Persistence
- **Storage**: In-memory only (session-based)
- **Lifecycle**: Data cleared on app termination
- **Security**: No sensitive data written to disk

## Build and Deployment

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or newer
- Android SDK API 30-34
- Gradle 8.7+
- YOLOv8n ONNX model file

### Build Configuration
```kotlin
android {
    compileSdk = 34
    minSdk = 30
    targetSdk = 34
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    
    kotlinOptions {
        jvmTarget = "1.8"
    }
}
```

### Asset Requirements
- **Model File**: `app/src/main/assets/yolov8n.onnx`
- **Size**: ~12.2MB
- **Format**: ONNX FP32

## Testing Strategy

### Current State
- **Unit Tests**: Not implemented
- **Integration Tests**: Not implemented  
- **UI Tests**: Not implemented

### Recommended Testing
- **Model Inference Tests**: Validate detection accuracy
- **UI Component Tests**: RecyclerView behavior, camera permissions
- **Performance Tests**: Inference timing, memory usage
- **Device Compatibility**: Various Android API levels and devices

## Known Limitations

### Technical Constraints
- **Model Size**: 12.2MB may be large for some devices
- **CPU-Only Inference**: No GPU acceleration implemented
- **API Level**: Requires Android 11+ (API 30)
- **Camera Dependency**: Requires rear-facing camera

### Functional Limitations
- **Session-Based**: No data persistence between app sessions
- **Single Detection**: One item per detection (no multiple instance counting)
- **Limited Classes**: Only 80 COCO classes supported
- **Static Pricing**: No dynamic price lookup

## Future Enhancements

### Short Term
- **Data Persistence**: SQLite/Room database integration
- **Export Functionality**: CSV/JSON inventory export
- **Batch Operations**: Multiple item selection/deletion
- **Custom Categories**: User-defined item categories

### Medium Term
- **GPU Acceleration**: ExecuTorch integration with QNN backend
- **Additional Models**: Support for custom/specialized models
- **Cloud Backup**: Optional encrypted cloud storage
- **Barcode Support**: QR/barcode scanning integration

### Long Term
- **Multi-Platform**: iOS version development
- **Enterprise Features**: Team sharing, admin controls
- **AI Improvements**: Custom model training, fine-tuning
- **Insurance Integration**: Direct insurance provider API connections

## References

- [YOLOv8 Documentation](https://docs.ultralytics.com/)
- [ONNX Runtime Mobile](https://onnxruntime.ai/docs/tutorials/mobile/)
- [CameraX Documentation](https://developer.android.com/training/camerax)
- [Material Design 3](https://m3.material.io/)