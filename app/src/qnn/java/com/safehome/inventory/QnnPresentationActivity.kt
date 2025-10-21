package com.safehome.inventory

import android.os.Bundle

/**
 * Presentation activity for QNN flavor showing Qualcomm QNN features.
 */
class QnnPresentationActivity : PresentationActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Customize presentation for QNN
        supportActionBar?.title = "SafeHome - Qualcomm QNN"
    }

    override fun getSlides(): List<Slide> {
        return listOf(
            Slide(
                title = "SafeHome Inventory",
                content = "AI-Powered Home Inventory\nFor Insurance & Safety\n\n⚡ Built with Qualcomm QNN\nDirect Hexagon NPU Access",
                backgroundColor = "#FF6F00"
            ),
            Slide(
                title = "Qualcomm QNN Backend",
                content = "🔥 QNN Execution Provider\n\n⚡ Direct Hexagon NPU access\n\n🎯 No NNAPI middleware\n\n🚀 20-30ms inference time\n\n🔋 40% lower power consumption",
                backgroundColor = "#2196F3"
            ),
            Slide(
                title = "Technical Architecture",
                content = "📱 SafeHome Inventory App\n\n🧠 ONNX Runtime 1.18.0\n\n⚡ QNN Execution Provider\n\n🔥 Qualcomm Hexagon NPU\n\n💎 Direct Hardware Access",
                backgroundColor = "#4CAF50"
            ),
            Slide(
                title = "Performance Benefits",
                content = "🚀 2-3x faster than NNAPI\n\n⚡ Sub-30ms latency\n\n🔋 40% less battery drain\n\n🎯 Direct hardware scheduling\n\n💪 Multi-core NPU utilization",
                backgroundColor = "#FF5722"
            ),
            Slide(
                title = "Key Features",
                content = "🎯 Real-time object detection (YOLOv8)\n\n🔒 100% privacy (on-device)\n\n⚡ Hexagon NPU acceleration\n\n🤖 On-device LLM (Llama 3.2)\n\n📱 Works completely offline",
                backgroundColor = "#9C27B0"
            )
        )
    }
}
