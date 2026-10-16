package com.fortunegem.mosaic.presentation.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.fortunegem.mosaic.R
import com.fortunegem.mosaic.core.di.ServiceLocator
import com.fortunegem.mosaic.core.ui.PressAnimator
import com.fortunegem.mosaic.databinding.DialogGemGalleryBinding
import com.fortunegem.mosaic.presentation.menu.ArtworkChip

class GemGalleryDialog : DialogFragment() {

    private var _binding: DialogGemGalleryBinding? = null
    private var adapter: GalleryAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Theme_FortuneMosaicGem_Dialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val created = DialogGemGalleryBinding.inflate(inflater, container, false)
        _binding = created
        return created.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bound = _binding ?: return
        val listAdapter = GalleryAdapter()
        adapter = listAdapter
        bound.galleryList.layoutManager = LinearLayoutManager(requireContext())
        bound.galleryList.adapter = listAdapter
        listAdapter.submit(collectChips())
        bound.galleryCloseButton.setOnClickListener {
            PressAnimator.pulse(bound.galleryCloseButton)
            dismissAllowingStateLoss()
        }
    }

    private fun collectChips(): List<ArtworkChip> {
        val context = requireContext()
        val artworks = ServiceLocator.artworkRepository(context).artworks()
        val progress = ServiceLocator.progressRepository(context)
        val unlocked = progress.unlockedArtwork()
        return artworks.map { artwork ->
            ArtworkChip(
                index = artwork.index,
                nameRes = artwork.nameRes,
                gemRes = artwork.gemRes,
                unlocked = artwork.index <= unlocked,
                stars = progress.starsFor(artwork.index)
            )
        }
    }

    override fun onDestroyView() {
        val bound = _binding
        bound?.galleryList?.adapter = null
        adapter = null
        _binding = null
        super.onDestroyView()
    }

    companion object {
        const val TAG = "gallery_dialog"
    }
}
