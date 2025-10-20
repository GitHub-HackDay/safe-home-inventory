# Build Instructions - SafeHome Inventory

## Prerequisites

### Required Software
- **Android Studio**: Hedgehog (2023.1.1) or newer
- **JDK**: OpenJDK 8 or higher
- **Android SDK**: API levels 30-34
- **Gradle**: 8.7+ (included in project)

### Hardware Requirements
- **Development Machine**: 8GB+ RAM recommended
- **Target Device**: Android 11+ (API 30) with camera
- **Recommended Test Device**: Samsung Galaxy S25 or equivalent

### Model Requirements
- **YOLOv8n ONNX Model**: Required for object detection
- **File**: `yolov8n.onnx` (~12.2MB)
- **Format**: ONNX FP32
- **Source**: Ultralytics YOLOv8 repository

## Quick Start (5-Minute Setup)

### 1. Clone Repository
```bash
git clone https://github.com/GitHub-HackDay/safe-home-inventory.git
cd safe-home-inventory
```

### 2. Obtain YOLOv8 Model
```bash
# Download pre-converted ONNX model
wget https://github.com/ultralytics/yolov8/releases/download/v8.0.0/yolov8n.onnx

# Move to assets directory
mv yolov8n.onnx app/src/main/assets/
```

### 3. Build and Install
```bash
# Build debug APK
./gradlew assembleDebug

# Install on connected device
adb install app/build/outputs/apk/debug/app-debug.apk
```

### 4. Launch and Test
1. Open SafeHome Inventory app
2. Grant camera permission when prompted
3. Point camera at objects (laptop, phone, bottle, etc.)
4. Verify real-time detection and inventory updates

## Detailed Build Process

### Step 1: Environment Setup

#### Android Studio Installation
1. Download Android Studio from [developer.android.com](https://developer.android.com/studio)
2. Install with default settings
3. Open SDK Manager and ensure the following are installed:
   - Android SDK Platform 30, 31, 32, 33, 34
   - Android SDK Build-Tools 34.0.0
   - Android Emulator (optional)

#### SDK Configuration
```bash
# Set ANDROID_HOME environment variable
export ANDROID_HOME=$HOME/Android/Sdk
export PATH=$PATH:$ANDROID_HOME/tools:$ANDROID_HOME/platform-tools
```

### Step 2: Project Setup

#### Clone and Inspect
```bash
git clone https://github.com/GitHub-HackDay/safe-home-inventory.git
cd safe-home-inventory

# Verify project structure
ls -la
# Expected: app/, build.gradle.kts, settings.gradle.kts, gradlew
```

#### Gradle Wrapper Verification
```bash
# Make gradlew executable (Linux/Mac)
chmod +x gradlew

# Test Gradle wrapper
./gradlew --version
# Expected: Gradle 8.7+, Kotlin 1.9.23+
```

### Step 3: Model Acquisition

#### Option A: Download Pre-converted Model
```bash
# Create assets directory if not exists
mkdir -p app/src/main/assets

# Download YOLOv8n ONNX model
curl -L -o app/src/main/assets/yolov8n.onnx \
  https://github.com/ultralytics/yolov8/releases/download/v8.0.0/yolov8n.onnx
```

#### Option B: Convert PyTorch Model (Advanced)
```bash
# Install Python dependencies
pip install ultralytics onnx

# Convert model
python -c "
from ultralytics import YOLO
model = YOLO('yolov8n.pt')
model.export(format='onnx', imgsz=640)
"

# Move to assets
mv yolov8n.onnx app/src/main/assets/
```

#### Verify Model
```bash
# Check file exists and size
ls -lh app/src/main/assets/yolov8n.onnx
# Expected: ~12.2MB file
```

### Step 4: Build Configuration

#### Debug Build
```bash
# Clean previous builds
./gradlew clean

# Build debug APK
./gradlew assembleDebug

# Verify APK creation
ls -lh app/build/outputs/apk/debug/
# Expected: app-debug.apk (~20-25MB)
```

#### Release Build (Optional)
```bash
# Generate signed release APK
./gradlew assembleRelease

# Note: Requires signing configuration in build.gradle.kts
```

### Step 5: Installation and Testing

#### Device Installation
```bash
# List connected devices
adb devices

# Install debug APK
adb install app/build/outputs/apk/debug/app-debug.apk

# Force reinstall if needed
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

#### Emulator Installation (Alternative)
```bash
# Create AVD with API 30+
avd create -n test_device -k "system-images;android-30;google_apis;x86_64"

# Start emulator
emulator -avd test_device

# Install APK
adb install app/build/outputs/apk/debug/app-debug.apk
```

## Build Variants and Configuration

### Debug Configuration
```kotlin
// app/build.gradle.kts
buildTypes {
    debug {
        isDebuggable = true
        applicationIdSuffix = ".debug"
        versionNameSuffix = "-debug"
        isMinifyEnabled = false
    }
}
```

### Release Configuration
```kotlin
release {
    isMinifyEnabled = true
    isShrinkResources = true
    proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro"
    )
    // Signing config required for release builds
}
```

## Troubleshooting

### Common Build Issues

#### Issue: "Plugin com.android.application not found"
**Solution**: Ensure internet connection and Gradle can download plugins
```bash
./gradlew --refresh-dependencies
```

#### Issue: "yolov8n.onnx not found"
**Solution**: Verify model file location
```bash
ls -la app/src/main/assets/yolov8n.onnx
# File must exist and be ~12.2MB
```

#### Issue: "Minimum SDK version"
**Solution**: Use device/emulator with Android 11+ (API 30+)
```bash
adb shell getprop ro.build.version.sdk
# Must return 30 or higher
```

#### Issue: "Camera permission denied"
**Solution**: Grant permission manually or reinstall app
```bash
# Clear app data and reinstall
adb uninstall com.safehome.inventory
adb install app/build/outputs/apk/debug/app-debug.apk
```

### Performance Issues

#### Slow Inference Times
- **Check**: Device CPU performance
- **Solution**: Test on higher-end device or reduce inference frequency
```kotlin
// Reduce inference rate in MainActivity.kt
.setBackpressureStrategy(ImageAnalysis.STRATEGY_BLOCK_PRODUCER)
```

#### High Memory Usage
- **Check**: Available device RAM
- **Solution**: Clear other apps or use device with more RAM

## Development Workflow

### Code Style
```bash
# Format Kotlin code
./gradlew ktlintFormat

# Check code style
./gradlew ktlintCheck
```

### Testing
```bash
# Run unit tests (when implemented)
./gradlew test

# Run instrumented tests (when implemented)
./gradlew connectedAndroidTest
```

### Debugging
```bash
# Enable detailed logging
adb shell setprop log.tag.YoloV8Detector VERBOSE
adb logcat -s YoloV8Detector
```

## Continuous Integration

### GitHub Actions (Recommended)
```yaml
# .github/workflows/build.yml
name: Build APK
on: [push, pull_request]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v3
    - uses: actions/setup-java@v3
      with:
        java-version: '11'
        distribution: 'temurin'
    - name: Grant execute permission for gradlew
      run: chmod +x gradlew
    - name: Build with Gradle
      run: ./gradlew assembleDebug
```

## Distribution

### Internal Testing
1. Build signed APK with debug keystore
2. Share APK file directly with testers
3. Install via `adb install` or file manager

### Play Store (Future)
1. Configure release signing in `build.gradle.kts`
2. Generate signed bundle: `./gradlew bundleRelease`
3. Upload to Play Console

## Resource Requirements

### Development
- **Disk Space**: 5GB+ (Android SDK + project)
- **RAM**: 8GB+ recommended
- **CPU**: Modern multi-core processor

### Target Device
- **OS**: Android 11+ (API 30)
- **RAM**: 4GB+ recommended
- **Storage**: 50MB for app installation
- **Camera**: Rear-facing camera required

## Hackathon Context

This project was built in **6 hours** during the ExecuTorch Hackathon at GitHub HQ. The build process was optimized for rapid development and demonstration:

### Time Constraints
- **Initial Setup**: 30 minutes
- **Core Development**: 4 hours
- **Testing and Polish**: 1.5 hours

### Demo Requirements
- **Quick Setup**: 5-minute build process
- **Reliable Performance**: Consistent 170ms inference
- **Professional Polish**: Material Design UI

## Support

### Documentation
- Technical Specification: `docs/TECHNICAL_SPECIFICATION.md`
- Feature Documentation: `docs/FEATURES.md`
- ExecuTorch Notes: `docs/EXECUTORCH_REVIEW.md`

### Community
- GitHub Issues: [Report bugs and feature requests](https://github.com/GitHub-HackDay/safe-home-inventory/issues)
- Discussions: Share usage examples and improvements

---

**Built with ❤️ in 6 hours at ExecuTorch Hackathon**