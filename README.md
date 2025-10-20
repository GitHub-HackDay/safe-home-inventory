# SafeHome Inventory

> Privacy-first home cataloging with on-device AI

Built for **ExecuTorch Hackathon** @ GitHub HQ (October 20, 2025)

![Android](https://img.shields.io/badge/Android-11%2B-green)
![Model](https://img.shields.io/badge/Model-YOLOv8n-blue)
![Runtime](https://img.shields.io/badge/Runtime-ONNX-orange)

---

## 📱 What is SafeHome?

SafeHome Inventory is a **privacy-first mobile app** that helps you catalog your belongings using **on-device AI object detection**. Point your camera at objects and they're automatically added to your inventory with estimated values.

### Key Features

- 🎥 **Real-time detection** using YOLOv8 (~170ms per frame)
- 🔒 **100% on-device** processing - no cloud, no uploads
- ✏️ **Manual editing** - rename items, adjust counts
- 📊 **Smart grouping** - expandable lists by object type
- 💰 **Value estimation** - automatic price calculation
- 🏷️ **Custom naming** - "laptop" → "MacBook Pro"

---

## 🎬 Demo

1. Launch app → Grant camera permission
2. Point camera at objects (laptop, phone, bottle, etc.)
3. Watch items appear in inventory list
4. Tap group to expand/collapse individual items
5. Tap item to rename or delete
6. See total value update in real-time

---

## 🛠️ Technical Details

### Architecture
- **Language**: Kotlin
- **Min SDK**: API 30 (Android 11)
- **Camera**: CameraX 1.3.1
- **ML Runtime**: ONNXRuntime Mobile 1.18.0
- **UI**: Material Design 3

### Model
- **Type**: YOLOv8n (Nano)
- **Format**: ONNX FP32
- **Size**: 12.2 MB
- **Classes**: 80 COCO objects
- **Performance**: ~170ms inference time

### Features
- Individual item tracking with UUIDs
- Expandable inventory groups
- Real-time bounding box overlay
- 3-second deduplication cooldown
- Manual item name editing
- Per-item deletion

---

## 📦 Building

### Prerequisites
- Android Studio Hedgehog or newer
- Android SDK API 30-34
- Gradle 8.0+

### Steps
```bash
# Clone repository
git clone <repo-url>
cd safe-home-inventory

# Place YOLOv8n ONNX model
# Copy yolov8n.onnx to app/src/main/assets/

# Build APK
./gradlew assembleDebug

# Install on device
adb install app/build/outputs/apk/debug/app-debug.apk
```

---

## 📚 Documentation

Detailed documentation available in `docs/`:
- `BUILD_INSTRUCTIONS.md` - Complete hackathon build guide
- `FEATURES.md` - Full feature list and technical specs
- `EXECUTORCH_REVIEW.md` - ExecuTorch architecture notes
- `MODEL_STATUS.md` - YOLOv8 model preparation
- `DEVICE_INFO.md` - Samsung S25 specifications

---

## 🏆 Hackathon Context

Built in **6 hours** at the ExecuTorch Hackathon focused on:
- ✅ On-device AI inference
- ✅ Real-world use case (insurance/moving)
- ✅ Privacy-first architecture
- ✅ Polished user experience
- ✅ Production-ready code quality

---

## 🎯 Use Cases

### Home Insurance
- Document valuables for insurance claims
- Estimated total value calculation
- Privacy-preserving (no cloud uploads)

### Moving/Relocation
- Track belongings during move
- Verify nothing lost
- Individual item accountability

### Estate Planning
- Catalog assets
- Share inventory with family
- Maintain privacy

---

## 📄 License

MIT License - See LICENSE file for details

---

## 🙏 Acknowledgments

- **ExecuTorch Team** - On-device AI framework
- **Qualcomm** - AI Hub and QNN backend
- **Ultralytics** - YOLOv8 model
- **GitHub** - Hosting the hackathon

---

**Built with ❤️ in 6 hours**
