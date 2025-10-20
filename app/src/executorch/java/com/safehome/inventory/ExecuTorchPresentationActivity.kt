package com.safehome.inventory

/**
 * ExecuTorch flavor presentation
 */
class ExecuTorchPresentationActivity : PresentationActivity() {

    override fun getSlides(): List<Slide> {
        return listOf(
            Slide(
                title = "SafeHome Inventory",
                content = "AI-Powered Home Inventory\nFor Insurance & Safety\n\n🔥 ExecuTorch Hackathon 2025",
                backgroundColor = "#FF6F00"
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
                title = "ExecuTorch Runtime",
                content = "This version demonstrates:\n\n🔥 ExecuTorch architecture\n\n📱 PyTorch Mobile integration\n\n🎯 Modular detector design\n\n⚠️ YOLOv8 conversion challenges",
                backgroundColor = "#FF6F00"
            ),
            Slide(
                title = "Technical Learnings",
                content = "🧠 ExecuTorch requires static graphs\n\n⚡ YOLOv8 has dynamic operations\n\n🔧 Built abstraction layer\n\n💡 Production would need simpler model\n\n📊 MobileNetV2-SSD as alternative",
                backgroundColor = "#9C27B0"
            ),
            Slide(
                title = "Architecture Highlights",
                content = "🏗️ Product Flavor pattern\n\n🔌 ObjectDetector interface\n\n🎨 Runtime-specific branding\n\n📦 Swappable ML backends\n\n✨ Same codebase, dual runtimes",
                backgroundColor = "#2196F3"
            ),
            Slide(
                title = "Why This Matters",
                content = "🔬 ExecuTorch is cutting-edge\n\n⚡ Optimized for mobile/edge\n\n🎯 Not all models compatible yet\n\n💪 Architecture shows flexibility\n\n🚀 Ready for future ExecuTorch models",
                backgroundColor = "#4CAF50"
            ),
            Slide(
                title = "Live Demo",
                content = "👉 Check ONNX version for working demo\n\n🏗️ This shows ExecuTorch integration pattern\n\n📱 Same UI, different runtime\n\n🎨 Orange branding = ExecuTorch",
                backgroundColor = "#FF9800"
            )
        )
    }
}
