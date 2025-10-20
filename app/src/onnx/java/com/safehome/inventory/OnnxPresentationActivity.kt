package com.safehome.inventory

/**
 * ONNX Runtime flavor presentation
 */
class OnnxPresentationActivity : PresentationActivity() {

    override fun getSlides(): List<Slide> {
        return listOf(
            Slide(
                title = "SafeHome Inventory",
                content = "AI-Powered Home Inventory\nFor Insurance & Safety\n\n🏠 ExecuTorch Hackathon 2025",
                backgroundColor = "#2196F3"
            ),
            Slide(
                title = "The Problem",
                content = "💔 Natural disasters destroy homes\n\n😰 Most people can't remember what they owned\n\n📋 Insurance claims are difficult without proof",
                backgroundColor = "#FF5722"
            ),
            Slide(
                title = "Our Solution",
                content = "📱 Point camera at your belongings\n\n🤖 AI automatically identifies items\n\n📸 Captures photos with bounding boxes\n\n💰 Tracks estimated values",
                backgroundColor = "#4CAF50"
            ),
            Slide(
                title = "ONNX Runtime",
                content = "This version uses:\n\n⚡ ONNX Runtime Mobile\n\n🎯 YOLOv8n model (6.3 MB)\n\n📊 80 COCO object classes\n\n✅ Real-time detection (~30ms)",
                backgroundColor = "#2196F3"
            ),
            Slide(
                title = "Technical Architecture",
                content = "🔧 Android Product Flavors\n\n📦 Modular detector interface\n\n🎨 Flavor-specific branding\n\n💾 Photo capture & storage\n\n📄 PDF export for insurance",
                backgroundColor = "#9C27B0"
            ),
            Slide(
                title = "Live Demo",
                content = "👉 Point camera at objects\n\n👆 Tap to add to inventory\n\n✏️ Highlight to force-add\n\n📸 Photos auto-captured\n\n📊 Export to PDF",
                backgroundColor = "#FF9800"
            ),
            Slide(
                title = "Impact",
                content = "🎯 Helps disaster victims\n\n💪 Empowers insurance claims\n\n📱 Works offline\n\n🆓 Free & open source",
                backgroundColor = "#4CAF50"
            )
        )
    }
}
