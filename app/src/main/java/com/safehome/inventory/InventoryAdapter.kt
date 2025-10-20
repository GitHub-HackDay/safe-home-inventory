package com.safehome.inventory

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

sealed class InventoryListItem {
    data class GroupHeader(val group: InventoryItemGroup) : InventoryListItem()
    data class IndividualItem(val item: TrackedItem, val groupClassName: String) : InventoryListItem()
}

class InventoryAdapter(
    private val onGroupClick: (String) -> Unit,
    private val onItemNameEdit: (String, String) -> Unit,
    private val onItemPriceEdit: (String, Double) -> Unit,
    private val onItemDelete: (String) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var listItems = listOf<InventoryListItem>()
    private var groups = listOf<InventoryItemGroup>()

    companion object {
        const val VIEW_TYPE_GROUP = 0
        const val VIEW_TYPE_ITEM = 1
    }

    class GroupViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameText: TextView = view.findViewById(R.id.groupNameText)
        val valueText: TextView = view.findViewById(R.id.groupValueText)
    }

    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val photoThumb: android.widget.ImageView = view.findViewById(R.id.itemPhotoThumb)
        val nameText: TextView = view.findViewById(R.id.itemNameText)
        val valueText: TextView = view.findViewById(R.id.itemValueText)
    }

    override fun getItemViewType(position: Int): Int {
        return when (listItems[position]) {
            is InventoryListItem.GroupHeader -> VIEW_TYPE_GROUP
            is InventoryListItem.IndividualItem -> VIEW_TYPE_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return when (viewType) {
            VIEW_TYPE_GROUP -> {
                val view = inflater.inflate(R.layout.item_inventory_group, parent, false)
                GroupViewHolder(view)
            }
            VIEW_TYPE_ITEM -> {
                val view = inflater.inflate(R.layout.item_inventory_individual, parent, false)
                ItemViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = listItems[position]) {
            is InventoryListItem.GroupHeader -> {
                val groupHolder = holder as GroupViewHolder
                val group = item.group
                val expandIcon = if (group.isExpanded) "▼" else "▶"

                // Show better name for manual items
                val displayName = if (group.className == "manual") {
                    "Manually Added Items"
                } else {
                    group.className
                }

                groupHolder.nameText.text = "$expandIcon $displayName × ${group.count}"
                groupHolder.valueText.text = "$${group.totalValue.toInt()}"

                groupHolder.itemView.setOnClickListener {
                    onGroupClick(group.className)
                }
            }
            is InventoryListItem.IndividualItem -> {
                val itemHolder = holder as ItemViewHolder
                val trackedItem = item.item

                // Don't show index for manual items (they have unique custom names)
                val displayText = if (item.groupClassName == "manual") {
                    trackedItem.displayName
                } else {
                    val index = groups.find { it.className == item.groupClassName }
                        ?.items?.indexOf(trackedItem)?.plus(1) ?: 1
                    "${trackedItem.displayName} #$index"
                }

                itemHolder.nameText.text = displayText
                itemHolder.valueText.text = "$${trackedItem.pricePerItem.toInt()}"

                // Load photo thumbnail
                if (trackedItem.hasPhoto && trackedItem.photoPath != null) {
                    val bitmap = android.graphics.BitmapFactory.decodeFile(trackedItem.photoPath)
                    itemHolder.photoThumb.setImageBitmap(bitmap)
                    itemHolder.photoThumb.visibility = View.VISIBLE
                } else {
                    itemHolder.photoThumb.setImageDrawable(null)
                    itemHolder.photoThumb.setBackgroundColor(android.graphics.Color.parseColor("#E0E0E0"))
                    itemHolder.photoThumb.visibility = View.VISIBLE
                }

                // Tap photo to view full size
                itemHolder.photoThumb.setOnClickListener {
                    if (trackedItem.hasPhoto && trackedItem.photoPath != null) {
                        // TODO: Show full-size photo dialog
                    }
                }

                // Tap name to edit
                itemHolder.nameText.setOnClickListener {
                    showItemNameEditDialog(itemHolder.itemView, trackedItem)
                }

                // Tap price to edit
                itemHolder.valueText.setOnClickListener {
                    showPriceEditDialog(itemHolder.itemView, trackedItem)
                }
            }
        }
    }

    private fun showItemNameEditDialog(view: View, item: TrackedItem) {
        val context = view.context
        val editText = EditText(context).apply {
            setText(item.displayName)
            hint = "Item name (e.g., MacBook Pro)"
        }

        AlertDialog.Builder(context)
            .setTitle("Edit Item Name")
            .setView(editText)
            .setPositiveButton("Save") { _, _ ->
                val newName = editText.text.toString().trim()
                if (newName.isNotEmpty()) {
                    onItemNameEdit(item.id, newName)
                }
            }
            .setNegativeButton("Delete") { _, _ ->
                onItemDelete(item.id)
            }
            .setNeutralButton("Cancel", null)
            .show()
    }

    private fun showPriceEditDialog(view: View, item: TrackedItem) {
        val context = view.context
        val editText = EditText(context).apply {
            setText(item.pricePerItem.toInt().toString())
            hint = "Price in dollars"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }

        AlertDialog.Builder(context)
            .setTitle("Edit Price")
            .setMessage("Estimated value for ${item.displayName}")
            .setView(editText)
            .setPositiveButton("Save") { _, _ ->
                val priceText = editText.text.toString().trim()
                val newPrice = priceText.toDoubleOrNull()
                if (newPrice != null && newPrice >= 0) {
                    onItemPriceEdit(item.id, newPrice)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun getItemCount() = listItems.size

    fun updateGroups(newGroups: List<InventoryItemGroup>) {
        groups = newGroups
        listItems = buildListItems(newGroups)
        notifyDataSetChanged()
    }

    private fun buildListItems(groups: List<InventoryItemGroup>): List<InventoryListItem> {
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
}
