package com.safehome.inventory

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

/**
 * Swipeable presentation activity for hackathon demo.
 * Shows different slides for ONNX vs ExecuTorch flavors.
 */
open class PresentationActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var closeButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_presentation)

        viewPager = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.tabLayout)
        closeButton = findViewById(R.id.closeButton)

        val slides = getSlides()
        val adapter = SlideAdapter(slides)
        viewPager.adapter = adapter

        // Connect tab layout to ViewPager
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            // Optional: customize tab appearance
        }.attach()

        closeButton.setOnClickListener {
            finish()
        }
    }

    /**
     * Override this in flavor-specific source sets to provide custom slides
     */
    protected open fun getSlides(): List<Slide> {
        return listOf(
            Slide(
                title = "SafeHome Inventory",
                content = "AI-powered home inventory\nfor insurance and safety",
                backgroundColor = "#2196F3"
            ),
            Slide(
                title = "Features",
                content = "• Real-time object detection\n• Photo capture\n• PDF export\n• Custom pricing",
                backgroundColor = "#4CAF50"
            )
        )
    }

    data class Slide(
        val title: String,
        val content: String,
        val backgroundColor: String
    )

    private class SlideAdapter(private val slides: List<Slide>) :
        RecyclerView.Adapter<SlideAdapter.SlideViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SlideViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_slide, parent, false)
            return SlideViewHolder(view)
        }

        override fun onBindViewHolder(holder: SlideViewHolder, position: Int) {
            holder.bind(slides[position])
        }

        override fun getItemCount() = slides.size

        class SlideViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val titleText: TextView = itemView.findViewById(R.id.slideTitle)
            private val contentText: TextView = itemView.findViewById(R.id.slideContent)
            private val container: View = itemView.findViewById(R.id.slideContainer)

            fun bind(slide: Slide) {
                titleText.text = slide.title
                contentText.text = slide.content
                container.setBackgroundColor(android.graphics.Color.parseColor(slide.backgroundColor))
            }
        }
    }
}
