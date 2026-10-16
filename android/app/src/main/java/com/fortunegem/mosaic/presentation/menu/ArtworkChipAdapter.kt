package com.fortunegem.mosaic.presentation.menu

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.fortunegem.mosaic.R
import com.fortunegem.mosaic.core.ext.ViewExt
import com.fortunegem.mosaic.databinding.ItemArtworkChipBinding

class ArtworkChipAdapter(
    private val onChipTapped: (Int) -> Unit
) : RecyclerView.Adapter<ArtworkChipAdapter.ChipHolder>() {

    private val items = ArrayList<ArtworkChip>()

    fun submit(chips: List<ArtworkChip>) {
        items.clear()
        items.addAll(chips)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChipHolder {
        val binding = ItemArtworkChipBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ChipHolder(binding)
    }

    override fun onBindViewHolder(holder: ChipHolder, position: Int) {
        holder.bind(items[position], position, onChipTapped)
    }

    override fun getItemCount(): Int = items.size

    class ChipHolder(
        private val binding: ItemArtworkChipBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(chip: ArtworkChip, position: Int, onChipTapped: (Int) -> Unit) {
            val context = binding.root.context
            binding.chipName.setText(chip.nameRes)
            binding.chipGem.setImageResource(chip.gemRes)
            binding.chipGem.contentDescription = context.getString(chip.nameRes)
            binding.chipLock.visibility = if (chip.unlocked) View.GONE else View.VISIBLE
            binding.chipContent.alpha = if (chip.unlocked) 1f else 0.45f
            val stateRes = if (chip.unlocked) R.string.state_unlocked else R.string.state_locked
            ViewExt.describeState(binding.chipCard, context.getString(stateRes))
            binding.chipCard.isEnabled = chip.unlocked
            binding.chipCard.setOnClickListener {
                if (chip.unlocked) onChipTapped(position)
            }
        }
    }
}
