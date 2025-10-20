package com.safehome.inventory

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.io.FileOutputStream

class PhotoManager(private val context: Context) {

    private val photosDir: File by lazy {
        File(context.filesDir, "inventory_photos").apply {
            if (!exists()) mkdirs()
        }
    }

    /**
     * Save full frame and cropped product image
     * Returns path to cropped image
     */
    fun saveItemPhoto(
        itemId: String,
        fullBitmap: Bitmap,
        boundingBox: BoundingBox
    ): String? {
        try {
            // Crop to bounding box with 10% padding
            val croppedBitmap = cropWithPadding(fullBitmap, boundingBox, paddingPercent = 0.1f)

            // Save cropped image
            val photoFile = File(photosDir, "${itemId}_cropped.jpg")
            FileOutputStream(photoFile).use { out ->
                croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
            }

            croppedBitmap.recycle()

            return photoFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    /**
     * Crop bitmap to bounding box with padding
     */
    private fun cropWithPadding(
        bitmap: Bitmap,
        box: BoundingBox,
        paddingPercent: Float = 0.1f
    ): Bitmap {
        val width = box.x2 - box.x1
        val height = box.y2 - box.y1

        // Add padding
        val padX = width * paddingPercent
        val padY = height * paddingPercent

        // Calculate crop region with bounds checking
        val x = maxOf(0f, box.x1 - padX).toInt()
        val y = maxOf(0f, box.y1 - padY).toInt()
        val w = minOf(bitmap.width - x, (width + 2 * padX).toInt())
        val h = minOf(bitmap.height - y, (height + 2 * padY).toInt())

        return Bitmap.createBitmap(bitmap, x, y, w, h)
    }

    /**
     * Load photo from path
     */
    fun loadPhoto(photoPath: String): Bitmap? {
        return try {
            BitmapFactory.decodeFile(photoPath)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Delete photo file
     */
    fun deletePhoto(photoPath: String): Boolean {
        return try {
            File(photoPath).delete()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Get all photo files
     */
    fun getAllPhotos(): List<File> {
        return photosDir.listFiles()?.toList() ?: emptyList()
    }

    /**
     * Clear all photos
     */
    fun clearAllPhotos() {
        photosDir.listFiles()?.forEach { it.delete() }
    }
}
