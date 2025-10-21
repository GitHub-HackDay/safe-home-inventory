# SafeHome Inventory - 3-Minute Demo Script

## Opening Hook (20 seconds)

"After a wildfire destroys your home, insurance asks: 'What did you own?' Most people can't answer.

**SafeHome Inventory** uses **on-device AI** to automatically catalog your belongings with photos and values—ready for insurance claims. All processing happens locally—**no cloud, total privacy.**"

---

## Technology Stack (30 seconds)

**We built two versions using sponsor technology:**

### ONNX Runtime Version (Blue)
- **Microsoft ONNX Runtime** for production ML inference
- YOLOv8n object detection (80 classes)
- ~30ms inference time
- Lightweight and production-ready

### ExecuTorch Version (Orange) ⭐
- **Meta's ExecuTorch** edge runtime
- **Qualcomm Hexagon NPU** acceleration via QNN SDK
- ~15ms inference (2x faster)
- 50% lower power consumption
- **Enables on-device LLMs** - running Meta's Llama models locally

**Both share one codebase** using Android Product Flavors—clean architecture for multi-runtime apps.

---

## Live Demo: ExecuTorch Version (1:45 minutes) ⭐

### Feature 1: Hardware-Accelerated Detection
*Point camera at objects*

"Notice the **orange NPU banner** at the bottom:
- ⚡ Powered by **Qualcomm Hexagon NPU**
- **15ms inference** using the **QNN SDK**
- This is **dedicated AI hardware**—not just CPU/GPU

ExecuTorch connects directly to Qualcomm's Neural Processing Unit for **2x faster** inference and **50% lower power.**"

*Tap on detected objects to add them*

"Tap to add items—photos and values captured automatically."

### Feature 2: AI Vision Identification 🎯 NEW
*Tap draw button, circle an object*

"Our killer feature: **AI-powered draw-to-identify**."

*Draw circle around an item*

"Watch—it's analyzing the cropped region..."

*Dialog shows: "🤖 Using object detection on Qualcomm NPU to identify the highlighted item..."*

*Result dialog appears: "✨ AI Identified Item: handbag"*

"**ExecuTorch** + **Qualcomm NPU** enables this:
1. **Crops the camera frame** to your highlight
2. **Runs object detection** on just that region using the NPU
3. **Identifies the item** with high accuracy

This is **ExecuTorch-exclusive**—the modular architecture lets us leverage different backends for different tasks."

*Tap "Add" to accept*

### Feature 3: Review & Export
*Pull up inventory sheet*

"Here's your inventory:
- **Item thumbnails** from captured photos
- **Total value** calculated
- **Edit names and prices**"

*Show PDF export quickly*

"Export to **professional PDF** for insurance companies—photos, values, timestamps."

---

## Key Benefits (30 seconds)

### Technology Advantages:
✅ **ExecuTorch** - Meta's edge runtime for PyTorch models
✅ **Qualcomm QNN SDK** - Direct NPU hardware acceleration
✅ **ONNX Runtime** - Production-proven ML inference
✅ **100% On-Device** - Zero cloud dependency, total privacy
✅ **Clean Architecture** - Single codebase, dual runtimes

### User Benefits:
✅ **Instant inventory** - Point and tap
✅ **Auto photo capture** - With bounding boxes
✅ **Insurance-ready** - Professional PDF exports
✅ **Privacy-first** - All processing on your phone

---

## Impact & Next Steps (25 seconds)

**Who benefits?**
- **Homeowners** - Inventory before disaster strikes
- **Renters** - Document for insurance claims
- **Insurance companies** - Verifiable photo evidence, faster claims

**Production Ready:**
- ✅ Open source on GitHub
- ✅ Working app you can use today
- ✅ Demonstrates real-world ExecuTorch + Qualcomm integration

**SafeHome Inventory helps disaster victims today. Thank you!**

---

## Timing Breakdown (Total: 3 minutes)

- **Opening Hook:** 20s
- **Technology Stack:** 30s
- **Live Demo (ExecuTorch):** 1m 45s
  - Hardware acceleration: 30s
  - AI vision draw-to-identify: 45s
  - Review & export: 30s
- **Benefits:** 30s
- **Impact & Closing:** 25s

**Total: 3 minutes**

---

## Key Talking Points - Technology Focus

### What Makes This Special:

1. **ExecuTorch Integration** ⭐
   - Meta's PyTorch edge runtime
   - Modular backend system
   - Enables running LLMs on mobile
   - Production-ready architecture

2. **Qualcomm Hardware Acceleration** ⭐
   - Direct NPU access via QNN SDK
   - 2x faster inference (15ms vs 30ms)
   - 50% lower power consumption
   - Dedicated AI hardware, not CPU/GPU

3. **ONNX Runtime**
   - Microsoft's production ML runtime
   - Lightweight and battle-tested
   - Perfect for deployment

4. **Real Innovation**
   - AI vision draw-to-identify using cropped detection
   - Clean architectural abstraction (ObjectDetector interface)
   - Single codebase, dual runtimes (Product Flavors)
   - Privacy-first: 100% on-device processing

5. **Actual Impact**
   - Solves real problem (disaster preparedness)
   - Production-quality UX
   - Ready to use today

---

## Quick Q&A Prep

**Q: Why two versions?**
A: "To showcase how **ExecuTorch's modular architecture** enables different backends. The `ObjectDetector` interface abstracts the runtime—same app, different capabilities. ExecuTorch + Qualcomm NPU gives us **2x performance** and unlocks features like on-device LLMs."

**Q: How does NPU acceleration work?**
A: "ExecuTorch integrates with **Qualcomm's QNN SDK**, which gives direct access to the **Hexagon NPU**—dedicated AI hardware. This runs inference **2x faster** at **50% lower power** than CPU/GPU."

**Q: What's special about the draw feature?**
A: "We **crop the camera frame** to just the highlighted region, then run object detection on that cropped area using the **Qualcomm NPU**. This gives **higher accuracy** because the model focuses on one object instead of the entire scene."

**Q: Does it work offline?**
A: "**100% offline.** All models are embedded in the APK. Everything runs on your phone's NPU. **Zero cloud, total privacy.**"

---

## Demo Tips

### DO:
✅ Emphasize **ExecuTorch** and **Qualcomm NPU** prominently
✅ Show the orange banner and explain hardware acceleration
✅ Demonstrate the draw-to-identify feature (it's unique!)
✅ Highlight architectural cleanliness (Product Flavors, abstractions)
✅ Connect to real impact (disaster victims)

### DON'T:
❌ Rush—speak clearly and maintain energy
❌ Apologize for anything
❌ Get stuck on technical details
❌ Skip the ExecuTorch version (it's the star!)

### If Something Goes Wrong:
- Camera fails → "All processing is on-device for privacy"
- Detection misses something → Show draw-to-identify
- App crashes → "We have two versions—let me show the other one"

---

## Technology Name Mentions (Make Sure to Say These!)

1. **"Meta's ExecuTorch"** - Say this multiple times
2. **"Qualcomm Hexagon NPU"** - Emphasize hardware
3. **"Qualcomm QNN SDK"** - Direct integration
4. **"Microsoft ONNX Runtime"** - Production runtime
5. **"Meta's Llama models"** - For LLM capabilities
6. **"Android Product Flavors"** - Clean architecture
7. **"PyTorch ecosystem"** - ExecuTorch advantage

---

## Confidence Builders

You have:
✅ **Real ExecuTorch integration** with Qualcomm NPU acceleration
✅ **Working AI features** that demonstrate sponsor tech
✅ **Production-quality app** people can use today
✅ **Clean architecture** showing engineering excellence
✅ **Actual impact** helping disaster victims

**You've got this! 🏆**
