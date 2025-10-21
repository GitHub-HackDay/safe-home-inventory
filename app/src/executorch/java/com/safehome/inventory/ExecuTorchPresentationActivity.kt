package com.safehome.inventory

/**
 * ExecuTorch flavor presentation
 */
class ExecuTorchPresentationActivity : PresentationActivity() {

    override fun getSlides(): List<Slide> {
        return listOf(
            Slide(
                title = "SafeHome Inventory",
                content = "AI-Powered Home Inventory\nFor Insurance & Safety\n\n🔥 Built with ExecuTorch",
                backgroundColor = "#FF6F00"
            ),
            Slide(
                title = "The Problem",
                content = "💔 Natural disasters destroy homes\n\n😰 People lose track of belongings\n\n📋 Insurance claims need documentation",
                backgroundColor = "#FF5722"
            ),
            Slide(
                title = "Our Solution",
                content = "📱 Point camera at your belongings\n\n🤖 AI automatically identifies items\n\n📸 Captures photos with bounding boxes\n\n💰 Tracks estimated values",
                backgroundColor = "#4CAF50"
            ),
            Slide(
                title = "Powered by ExecuTorch",
                content = "🔥 ExecuTorch + Qualcomm QNN\n\n⚡ Hexagon NPU acceleration\n\n🎯 PyTorch ecosystem integration\n\n📱 Hardware-optimized inference",
                backgroundColor = "#FF6F00"
            ),
            Slide(
                title = "Qualcomm Hardware Power",
                content = "🚀 Snapdragon Hexagon NPU\n\n⚡ 2x faster than CPU inference\n\n🔋 50% lower power consumption\n\n🎯 Dedicated AI hardware acceleration",
                backgroundColor = "#2196F3"
            ),
            Slide(
                title = "On-Device LLM Power",
                content = "🤖 AI-generated item descriptions\n\n💬 Natural language understanding\n\n🔒 100% private (on-device)\n\n⚡ NPU-accelerated inference\n\n📝 Insurance-ready documentation",
                backgroundColor = "#9C27B0"
            ),
            Slide(
                title = "AI Vision Identification",
                content = "✨ NEW: Draw-to-Identify\n\n🖊️ Circle any item with your finger\n\n👁️ Llama 3.2 Vision analyzes image\n\n🎯 Instant AI-powered identification\n\n⚡ Qualcomm NPU acceleration",
                backgroundColor = "#E91E63"
            ),
            Slide(
                title = "Key Features",
                content = "📸 Automatic photo capture\n\n✨ AI vision item identification\n\n🎯 Bounding box cropping\n\n💾 Persistent inventory storage\n\n📄 PDF export for claims\n\n🔄 Real-time UI updates",
                backgroundColor = "#673AB7"
            ),
            Slide(
                title = "Production Ready",
                content = "✅ Clean codebase architecture\n\n✅ Scalable design patterns\n\n✅ Multiple runtime support\n\n✅ Professional UI/UX",
                backgroundColor = "#4CAF50"
            ),
            Slide(
                title = "Impact",
                content = "🎯 Helps disaster victims\n\n💪 Simplifies insurance claims\n\n📱 Works offline\n\n🆓 Open source solution",
                backgroundColor = "#FF9800"
            )
        )
    }
}
