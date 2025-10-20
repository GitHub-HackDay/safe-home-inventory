package com.safehome.inventory

import android.content.Context
import android.content.Intent

/**
 * Factory for launching flavor-specific presentation activities
 */
object PresentationLauncher {
    /**
     * Must be implemented in each flavor's DetectorFactory
     */
    fun launch(context: Context) {
        val intent = DetectorFactory.getPresentationIntent(context)
        context.startActivity(intent)
    }
}

/**
 * Extension to DetectorFactory to provide presentation intent
 */
interface PresentationProvider {
    fun getPresentationIntent(context: Context): Intent
}
