package com.safehome.inventory

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * SafeHome Inventory - Privacy-First Home Cataloging
 *
 * Built for ExecuTorch Hackathon @ GitHub HQ (Oct 20, 2025)
 *
 * Tech Stack:
 * - PyTorch ExecuTorch for on-device inference
 * - Qualcomm QNN backend leveraging Snapdragon 8 Elite NPU
 * - YOLOv8 object detection (80 COCO classes)
 * - Samsung Galaxy S25 for hardware acceleration
 *
 * Key Feature: 100% on-device processing - no cloud, no privacy concerns
 */
class MainActivity : AppCompatActivity() {

    private lateinit var cameraExecutor: ExecutorService
    private lateinit var detector: YoloV8Detector
    private val inventoryManager = InventoryManager()
    private lateinit var overlayView: OverlayView
    private lateinit var inventoryAdapter: InventoryAdapter
    private lateinit var totalValueText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize detector
        detector = YoloV8Detector(this)

        // Set up UI
        overlayView = findViewById(R.id.overlayView)
        totalValueText = findViewById(R.id.totalValueText)

        // Set up RecyclerView
        val recyclerView = findViewById<RecyclerView>(R.id.inventoryList)
        inventoryAdapter = InventoryAdapter(
            onGroupClick = { className ->
                inventoryManager.toggleGroupExpansion(className)
                updateInventoryDisplay()
            },
            onItemNameEdit = { itemId, newName ->
                inventoryManager.updateItemName(itemId, newName)
                updateInventoryDisplay()
            },
            onItemDelete = { itemId ->
                inventoryManager.removeItem(itemId)
                updateInventoryDisplay()
            }
        )
        recyclerView.adapter = inventoryAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Clear button
        findViewById<MaterialButton>(R.id.clearButton).setOnClickListener {
            inventoryManager.clear()
            updateInventoryDisplay()
        }

        // Camera executor
        cameraExecutor = Executors.newSingleThreadExecutor()

        // Request camera permission
        if (hasCameraPermission()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST
            )
        }
    }

    private fun hasCameraPermission() =
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera()
            }
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(findViewById<PreviewView>(R.id.previewView).surfaceProvider)
                }

            // Image analysis
            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor) { imageProxy ->
                        processImage(imageProxy)
                    }
                }

            // Select back camera
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageAnalysis
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun processImage(imageProxy: ImageProxy) {
        // Use built-in toBitmap() from CameraX
        val bitmap = imageProxy.toBitmap()
        val rotated = rotateBitmap(bitmap, imageProxy.imageInfo.rotationDegrees.toFloat())

        // Run detection
        val detections = detector.detect(rotated)

        // Update overlay
        runOnUiThread {
            overlayView.updateDetections(detections)
        }

        // Update inventory
        val updated = inventoryManager.addDetections(detections)
        if (updated) {
            runOnUiThread {
                updateInventoryDisplay()
            }
        }

        imageProxy.close()
    }

    private fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degrees)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun updateInventoryDisplay() {
        val groups = inventoryManager.getInventoryGroups()
        inventoryAdapter.updateGroups(groups)
        totalValueText.text = "$${inventoryManager.getTotalValue().toInt()}"
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        detector.close()
    }

    companion object {
        private const val CAMERA_PERMISSION_REQUEST = 100
    }
}
