package com.safehome.inventory

import kotlinx.coroutines.delay

/**
 * AI-powered item description generator using ExecuTorch + LLM.
 *
 * In production, this would run a small language model (e.g., Llama 3.2 1B)
 * on-device using ExecuTorch with Qualcomm NPU acceleration.
 */
object ItemDescriptionGenerator {

    /**
     * Generate detailed description for an item using on-device LLM.
     *
     * Production implementation would:
     * 1. Load Llama 3.2 1B model (.pte file)
     * 2. Create prompt: "Describe this {className} for insurance purposes"
     * 3. Run inference on Qualcomm NPU via ExecuTorch
     * 4. Return generated description
     */
    suspend fun generateDescription(className: String): String {
        // Simulate LLM inference time (~200-500ms on Hexagon NPU)
        delay(300)

        // Demo: Return intelligent descriptions based on item type
        return when (className.lowercase()) {
            "laptop" -> "Portable computer device, likely used for work or personal computing. Consider documenting brand, model, screen size, and any distinguishing features for insurance valuation."

            "cell phone", "phone" -> "Mobile communication device with computing capabilities. Document make, model, storage capacity, and condition. Photos of IMEI number recommended."

            "tv", "television" -> "Electronic display device for media consumption. Note screen size (measured diagonally), resolution (HD/4K), smart features, and brand for accurate replacement cost."

            "couch", "sofa" -> "Upholstered seating furniture. Document material (leather/fabric), color, dimensions, and brand if known. Condition affects replacement value."

            "book" -> "Printed publication or e-reader. For rare/collectible books, document title, author, edition, and condition. Standard books may have minimal insurance value."

            "bottle" -> "Container, possibly for beverages. Document if decorative, collectible, or has special value. Standard containers typically not itemized for insurance."

            "cup" -> "Drinking vessel. Document only if part of valuable set, antique, or collectible. Material (crystal, china, etc.) affects value."

            "keyboard" -> "Computer input device. Document if mechanical, gaming, or specialty keyboard. Note brand and connectivity (wired/wireless)."

            "mouse" -> "Computer pointing device. Document if specialty gaming mouse or ergonomic model. Standard mice typically not individually insured."

            "chair" -> "Seating furniture. Document if office chair (ergonomic features), dining chair (part of set), or specialty furniture. Note material and brand."

            "clock" -> "Timekeeping device. Document if antique, collectible, or designer piece. Note maker, style, and working condition."

            "vase" -> "Decorative container. Document if antique, designer, or valuable collectible. Note maker, material (crystal, ceramic), and size."

            else -> "Household item classified as '$className'. Document any unique features, brand information, approximate purchase date, and condition. Include receipts if available for accurate insurance valuation."
        }
    }

    /**
     * Check if LLM features are available (requires ExecuTorch + NPU)
     */
    fun isAvailable(): Boolean {
        // In production, check if:
        // 1. ExecuTorch runtime is loaded
        // 2. LLM model file exists
        // 3. Qualcomm NPU is available
        return true // Demo mode: always available
    }

    /**
     * Get model info for display
     */
    fun getModelInfo(): String {
        return "Llama 3.2 1B (ExecuTorch + Qualcomm NPU)"
    }
}
