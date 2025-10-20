package com.safehome.inventory

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PdfExporter(private val context: Context) {

    companion object {
        private const val CHANNEL_ID = "pdf_export_channel"
        private const val NOTIFICATION_ID = 1001
    }

    fun exportInventory(
        groups: List<InventoryItemGroup>,
        totalValue: Double,
        callback: (Boolean, File?) -> Unit
    ) {
        try {
            // Create PDF document
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
            var page = pdfDocument.startPage(pageInfo)
            var canvas = page.canvas

            // Setup paint
            val titlePaint = Paint().apply {
                color = Color.BLACK
                textSize = 24f
                isFakeBoldText = true
            }

            val headerPaint = Paint().apply {
                color = Color.BLACK
                textSize = 16f
                isFakeBoldText = true
            }

            val textPaint = Paint().apply {
                color = Color.BLACK
                textSize = 12f
            }

            val linePaint = Paint().apply {
                color = Color.GRAY
                strokeWidth = 1f
            }

            var yPosition = 50f

            // Title
            canvas.drawText("SafeHome Inventory Report", 50f, yPosition, titlePaint)
            yPosition += 30f

            // Date
            val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.US)
            canvas.drawText("Generated: ${dateFormat.format(Date())}", 50f, yPosition, textPaint)
            yPosition += 30f

            // Total Value
            canvas.drawText("Total Estimated Value: $${totalValue.toInt()}", 50f, yPosition, headerPaint)
            yPosition += 40f

            // Line separator
            canvas.drawLine(50f, yPosition, 545f, yPosition, linePaint)
            yPosition += 20f

            // Inventory items
            for (group in groups) {
                // Check if we need a new page
                if (yPosition > 750f) {
                    pdfDocument.finishPage(page)
                    page = pdfDocument.startPage(pageInfo)
                    canvas = page.canvas
                    yPosition = 50f
                }

                // Group header
                val groupText = "${group.className} × ${group.count} = $${group.totalValue.toInt()}"
                canvas.drawText(groupText, 50f, yPosition, headerPaint)
                yPosition += 25f

                // Individual items (if expanded or if custom names/prices)
                for (item in group.items) {
                    if (yPosition > 750f) {
                        pdfDocument.finishPage(page)
                        page = pdfDocument.startPage(pageInfo)
                        canvas = page.canvas
                        yPosition = 50f
                    }

                    val itemText = "  • ${item.displayName}: $${item.pricePerItem.toInt()}"
                    canvas.drawText(itemText, 70f, yPosition, textPaint)
                    yPosition += 20f
                }

                yPosition += 10f
            }

            // Finish the page
            pdfDocument.finishPage(page)

            // Save to file
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val fileName = "SafeHome_Inventory_$timestamp.pdf"

            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, fileName)

            FileOutputStream(file).use { outputStream ->
                pdfDocument.writeTo(outputStream)
            }

            pdfDocument.close()

            // Show notification with open action
            showNotification(file)

            callback(true, file)
        } catch (e: Exception) {
            e.printStackTrace()
            callback(false, null)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "PDF Exports",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for PDF export completion"
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showNotification(file: File) {
        createNotificationChannel()

        // Create intent to open PDF
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val openIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build notification
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_save)
            .setContentTitle("PDF Export Complete")
            .setContentText("Tap to open ${file.name}")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(false)  // Keep notification visible
            .addAction(
                android.R.drawable.ic_menu_view,
                "Open",
                pendingIntent
            )
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}
