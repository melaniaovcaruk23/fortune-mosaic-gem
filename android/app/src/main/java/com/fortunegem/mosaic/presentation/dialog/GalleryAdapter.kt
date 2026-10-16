package com.fortunegem.mosaic.presentation.dialog

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.fortunegem.mosaic.R
import com.fortunegem.mosaic.core.ext.ViewExt
import com.fortunegem.mosaic.databinding.ItemGalleryGemBinding
import com.fortunegem.mosaic.presentation.menu.ArtworkChip

class GalleryAdapter : RecyclerView.Adapter<GalleryAdapter.GemHolder>() {

    private val items = ArrayList<ArtworkChip>()

    fun submit(chips: List<ArtworkChip>) {
        items.clear()
        items.addAll(chips)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GemHolder {
        val binding = ItemGalleryGemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return GemHolder(binding)
    }

    override fun onBindViewHolder(holder: GemHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class GemHolder(
        private val binding: ItemGalleryGemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(chip: ArtworkChip) {
            val context = binding.root.context
            binding.galleryName.setText(chip.nameRes)
            binding.galleryGem.setImageResource(chip.gemRes)
            binding.galleryGem.contentDescription = context.getString(chip.nameRes)
            binding.galleryGem.alpha = if (chip.unlocked) 1f else 0.45f
            val statusRes = if (chip.unlocked) R.string.state_unlocked else R.string.locked
            binding.galleryStatus.setText(statusRes)
            binding.galleryStars.text = context.getString(
                R.string.progress_fraction,
                chip.stars,
                3
            )
            ViewExt.describeState(binding.galleryCard, context.getString(statusRes))
        }
    }
}
