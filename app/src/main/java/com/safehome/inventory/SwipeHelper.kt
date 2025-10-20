package com.safehome.inventory

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

class SwipeHelper(
    private val onSwipeLeft: (Int) -> Unit,
    private val onSwipeRight: (Int) -> Unit
) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean = false

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.bindingAdapterPosition
        when (direction) {
            ItemTouchHelper.LEFT -> onSwipeLeft(position)
            ItemTouchHelper.RIGHT -> onSwipeRight(position)
        }
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            val itemView = viewHolder.itemView
            val context = itemView.context

            val paint = Paint()
            val background = RectF(
                itemView.left.toFloat(),
                itemView.top.toFloat(),
                itemView.right.toFloat(),
                itemView.bottom.toFloat()
            )

            // Swipe Right - Green "Add" background
            if (dX > 0) {
                paint.color = ContextCompat.getColor(context, android.R.color.holo_green_light)
                c.drawRect(background, paint)

                // Draw text
                paint.color = ContextCompat.getColor(context, android.R.color.white)
                paint.textSize = 48f
                paint.textAlign = Paint.Align.LEFT
                c.drawText("✓ Add", itemView.left + 50f, itemView.top + (itemView.height / 2f) + 15f, paint)
            }
            // Swipe Left - Red "Ignore" background
            else if (dX < 0) {
                paint.color = ContextCompat.getColor(context, android.R.color.holo_red_light)
                c.drawRect(background, paint)

                // Draw text
                paint.color = ContextCompat.getColor(context, android.R.color.white)
                paint.textSize = 48f
                paint.textAlign = Paint.Align.RIGHT
                c.drawText("✕ Ignore", itemView.right - 50f, itemView.top + (itemView.height / 2f) + 15f, paint)
            }
        }

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }

    override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {
        return 0.3f  // Require 30% swipe to trigger
    }
}
