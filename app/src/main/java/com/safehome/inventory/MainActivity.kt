package com.safehome.inventory

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
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
import com.google.android.material.floatingactionbutton.FloatingActionButton
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
    private lateinit var detector: ObjectDetector
    private val inventoryManager = InventoryManager()
    private lateinit var photoManager: PhotoManager
    private lateinit var overlayView: OverlayView
    private lateinit var drawingOverlay: DrawingOverlayView
    private lateinit var inventoryAdapter: InventoryAdapter
    private lateinit var totalValueText: TextView
    private var isDrawingMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize detector and photo manager
        detector = DetectorFactory.createDetector(this)
        photoManager = PhotoManager(this)
        inventoryManager.photoManager = photoManager

        // Set up UI
        overlayView = findViewById(R.id.overlayView)
        drawingOverlay = findViewById(R.id.drawingOverlay)
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
            onItemPriceEdit = { itemId, newPrice ->
                inventoryManager.updateItemPrice(itemId, newPrice)
                updateInventoryDisplay()
            },
            onItemDelete = { itemId ->
                inventoryManager.removeItem(itemId)
                updateInventoryDisplay()
            }
        )
        recyclerView.adapter = inventoryAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Set up swipe gestures
        val swipeHelper = SwipeHelper(
            onSwipeLeft = { position ->
                // Swipe left to ignore
                val groups = inventoryManager.getInventoryGroups()
                val items = buildFlatList(groups)
                if (position < items.size) {
                    when (val item = items[position]) {
                        is InventoryListItem.GroupHeader -> {
                            inventoryManager.ignoreClass(item.group.className)
                            updateInventoryDisplay()
                        }
                        is InventoryListItem.IndividualItem -> {
                            inventoryManager.removeItem(item.item.id)
                            updateInventoryDisplay()
                        }
                    }
                }
            },
            onSwipeRight = { position ->
                // Swipe right - already added, just dismiss swipe
                inventoryAdapter.notifyItemChanged(position)
            }
        )
        val itemTouchHelper = androidx.recyclerview.widget.ItemTouchHelper(swipeHelper)
        itemTouchHelper.attachToRecyclerView(recyclerView)

        // Clear button
        findViewById<MaterialButton>(R.id.clearButton).setOnClickListener {
            inventoryManager.clear()
            updateInventoryDisplay()
        }

        // Menu FAB
        findViewById<FloatingActionButton>(R.id.menuFab).setOnClickListener { view ->
            showMenu(view as FloatingActionButton)
        }

        // Draw FAB
        val drawFab = findViewById<FloatingActionButton>(R.id.drawFab)
        drawFab.setOnClickListener {
            toggleDrawingMode(drawFab)
        }

        // Set up drawing overlay
        drawingOverlay.setOnDrawingCompleteListener { path ->
            showAddItemDialog()
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

        // Update inventory with photo capture
        val updated = inventoryManager.addDetections(detections, rotated)
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

    private fun buildFlatList(groups: List<InventoryItemGroup>): List<InventoryListItem> {
        val items = mutableListOf<InventoryListItem>()
        for (group in groups) {
            items.add(InventoryListItem.GroupHeader(group))
            if (group.isExpanded) {
                for (item in group.items) {
                    items.add(InventoryListItem.IndividualItem(item, group.className))
                }
            }
        }
        return items
    }

    private fun showMenu(anchorView: FloatingActionButton) {
        val popup = PopupMenu(this, anchorView)
        popup.menuInflater.inflate(R.menu.main_menu, popup.menu)

        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_photo_gallery -> {
                    showPhotoGallery()
                    true
                }
                R.id.menu_export_pdf -> {
                    exportToPdf()
                    true
                }
                R.id.menu_clear_ignored -> {
                    inventoryManager.clearIgnoredItems()
                    Toast.makeText(this, "Ignored items cleared", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.menu_about -> {
                    showAbout()
                    true
                }
                else -> false
            }
        }

        popup.show()
    }

    private fun exportToPdf() {
        val pdfExporter = PdfExporter(this)
        val groups = inventoryManager.getInventoryGroups()
        val totalValue = inventoryManager.getTotalValue()

        pdfExporter.exportInventory(groups, totalValue) { success, file ->
            runOnUiThread {
                if (success && file != null) {
                    Toast.makeText(
                        this,
                        "PDF saved to ${file.absolutePath}",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(
                        this,
                        "Failed to export PDF",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun showPhotoGallery() {
        val groups = inventoryManager.getInventoryGroups()
        val itemsWithPhotos = groups.flatMap { it.items }.filter { it.hasPhoto }

        if (itemsWithPhotos.isEmpty()) {
            Toast.makeText(this, "No photos yet! Point camera at items to capture.", Toast.LENGTH_LONG).show()
            return
        }

        // Create simple grid view dialog
        val gridView = android.widget.GridView(this).apply {
            numColumns = 3
            verticalSpacing = 8
            horizontalSpacing = 8
            setPadding(16, 16, 16, 16)
        }

        // Simple adapter for grid
        gridView.adapter = object : android.widget.BaseAdapter() {
            override fun getCount() = itemsWithPhotos.size
            override fun getItem(position: Int) = itemsWithPhotos[position]
            override fun getItemId(position: Int) = position.toLong()

            override fun getView(position: Int, convertView: View?, parent: android.view.ViewGroup?): View {
                val imageView = (convertView as? android.widget.ImageView) ?: android.widget.ImageView(this@MainActivity).apply {
                    layoutParams = android.widget.AbsListView.LayoutParams(200, 200)
                    scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
                }

                val item = itemsWithPhotos[position]
                val bitmap = android.graphics.BitmapFactory.decodeFile(item.photoPath)
                imageView.setImageBitmap(bitmap)

                return imageView
            }
        }

        // Show dialog
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Photo Gallery (${itemsWithPhotos.size} photos)")
            .setView(gridView)
            .setPositiveButton("Close", null)
            .show()
    }

    private fun showAbout() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_about, null)
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun toggleDrawingMode(fab: FloatingActionButton) {
        isDrawingMode = !isDrawingMode
        drawingOverlay.setDrawingEnabled(isDrawingMode)

        if (isDrawingMode) {
            fab.backgroundTintList = android.content.res.ColorStateList.valueOf(
                android.graphics.Color.parseColor("#4CAF50")  // Green when active
            )
            Toast.makeText(this, "Draw mode ON - Circle an item to add", Toast.LENGTH_SHORT).show()
        } else {
            fab.backgroundTintList = android.content.res.ColorStateList.valueOf(
                android.graphics.Color.parseColor("#FF9800")  // Orange when inactive
            )
            drawingOverlay.clearDrawing()
            Toast.makeText(this, "Draw mode OFF", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showAddItemDialog() {
        val dialogView = layoutInflater.inflate(android.R.layout.simple_list_item_2, null)
        val nameInput = android.widget.EditText(this).apply {
            hint = "Item name (e.g., Vintage Chair)"
        }
        val priceInput = android.widget.EditText(this).apply {
            hint = "Estimated value (dollars)"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }

        val layout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(50, 20, 50, 20)
            addView(nameInput)
            addView(priceInput)
        }

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Add Item to Inventory")
            .setMessage("Enter details for the circled item")
            .setView(layout)
            .setPositiveButton("Add") { _, _ ->
                val name = nameInput.text.toString().trim()
                val priceText = priceInput.text.toString().trim()
                val price = priceText.toDoubleOrNull() ?: 0.0

                if (name.isNotEmpty() && price > 0) {
                    // Add manually labeled item
                    val trackedItem = TrackedItem(
                        className = "manual",
                        customName = name,
                        pricePerItem = price,
                        notes = "Manually added"
                    )
                    inventoryManager.addManualItem(trackedItem)
                    updateInventoryDisplay()

                    Toast.makeText(this, "$name added to inventory", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Invalid name or price", Toast.LENGTH_SHORT).show()
                }

                // Clear drawing and stay in draw mode
                drawingOverlay.clearDrawing()
            }
            .setNegativeButton("Cancel") { _, _ ->
                drawingOverlay.clearDrawing()
            }
            .setOnCancelListener {
                drawingOverlay.clearDrawing()
            }
            .show()
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
