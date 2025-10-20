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
                content = "🔥 Edge-optimized AI inference\n\n⚡ Lightweight mobile runtime\n\n🎯 PyTorch ecosystem integration\n\n📱 On-device processing",
                backgroundColor = "#FF6F00"
            ),
            Slide(
                title = "Smart Architecture",
                content = "🏗️ Modular detector interface\n\n🔌 Pluggable ML backends\n\n🎨 Runtime-specific optimizations\n\n📦 Clean separation of concerns",
                backgroundColor = "#2196F3"
            ),
            Slide(
                title = "Key Features",
                content = "📸 Automatic photo capture\n\n🎯 Bounding box cropping\n\n💾 Persistent inventory storage\n\n📄 PDF export for claims\n\n🔄 Real-time UI updates",
                backgroundColor = "#9C27B0"
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
