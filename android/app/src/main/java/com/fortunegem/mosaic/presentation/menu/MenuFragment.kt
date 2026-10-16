package com.fortunegem.mosaic.presentation.menu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.fortunegem.mosaic.MainActivity
import com.fortunegem.mosaic.R
import com.fortunegem.mosaic.core.config.GameConfig
import com.fortunegem.mosaic.core.di.ViewModelFactory
import com.fortunegem.mosaic.core.ext.ViewExt
import com.fortunegem.mosaic.core.ui.PressAnimator
import com.fortunegem.mosaic.core.ui.ScoreFormatter
import com.fortunegem.mosaic.databinding.FragmentMenuBinding
import com.fortunegem.mosaic.presentation.dialog.GemGalleryDialog
import com.fortunegem.mosaic.presentation.dialog.OnboardingDialog
import com.fortunegem.mosaic.presentation.dialog.SettingsDialog
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MenuFragment : Fragment() {

    private var _binding: FragmentMenuBinding? = null
    private var adapter: ArtworkChipAdapter? = null

    private val viewModel: MenuViewModel by viewModels {
        ViewModelFactory(requireContext().applicationContext)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val created = FragmentMenuBinding.inflate(inflater, container, false)
        _binding = created
        return created.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bound = _binding ?: return
        val chipAdapter = ArtworkChipAdapter { position -> onChipTapped(position) }
        adapter = chipAdapter
        bound.menuArtworkStrip.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        bound.menuArtworkStrip.adapter = chipAdapter
        bound.menuPlayButton.setOnClickListener { onPlayTapped() }
        bound.menuGalleryButton.setOnClickListener { onGalleryTapped() }
        bound.menuSettingsButton.setOnClickListener { onSettingsTapped() }
        bound.menuHowtoButton.setOnClickListener { showOnboarding() }
        staggerEntrance()
        observeState()
    }

    private fun staggerEntrance() {
        val bound = _binding ?: return
        ViewExt.enterStaggered(bound.menuTitle, 0, GameConfig.STAGGER_MS)
        ViewExt.enterStaggered(bound.menuLevelBadge, 1, GameConfig.STAGGER_MS)
        ViewExt.enterStaggered(bound.menuHeroCard, 2, GameConfig.STAGGER_MS)
        ViewExt.enterStaggered(bound.menuStatColumn, 3, GameConfig.STAGGER_MS)
        ViewExt.enterStaggered(bound.menuStripHeader, 4, GameConfig.STAGGER_MS)
        ViewExt.enterStaggered(bound.menuArtworkStrip, 5, GameConfig.STAGGER_MS)
        ViewExt.enterStaggered(bound.menuHowtoButton, 6, GameConfig.STAGGER_MS)
        ViewExt.enterStaggered(bound.menuHint, 7, GameConfig.STAGGER_MS)
        ViewExt.enterStaggered(bound.menuPlayTile, 8, GameConfig.STAGGER_MS)
        ViewExt.enterStaggered(bound.menuGalleryButton, 9, GameConfig.STAGGER_MS)
        ViewExt.enterStaggered(bound.menuSettingsButton, 10, GameConfig.STAGGER_MS)
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collectLatest { state -> render(state) }
            }
        }
    }

    private fun render(state: MenuUiState) {
        val bound = _binding ?: return
        adapter?.submit(state.chips)
        bound.menuLevelBadge.bind(
            getString(R.string.workshop_level, state.level),
            getString(R.string.workshop_short)
        )
        if (state.restored > 0) {
            bound.menuStatArtworks.bind(
                getString(R.string.progress_fraction, state.restored, state.total),
                getString(R.string.label_artworks)
            )
        } else {
            bound.menuStatArtworks.bind(
                getString(R.string.stat_new),
                getString(R.string.label_artworks)
            )
        }
        if (state.bestScore > 0) {
            bound.menuStatBest.bind(
                ScoreFormatter.points(state.bestScore),
                getString(R.string.label_best)
            )
        } else {
            bound.menuStatBest.bind(
                getString(R.string.stat_new),
                getString(R.string.label_best)
            )
        }
    }

    private fun showOnboarding() {
        if (!isAdded) return
        if (parentFragmentManager.isStateSaved) return
        viewModel.markOnboardingSeen()
        OnboardingDialog().show(parentFragmentManager, OnboardingDialog.TAG)
    }

    private fun onChipTapped(position: Int) {
        val index = viewModel.selectArtwork(position)
        val bound = _binding ?: return
        PressAnimator.pulse(bound.menuPlayTile)
        startRound(index)
    }

    private fun onPlayTapped() {
        val bound = _binding ?: return
        PressAnimator.pulse(bound.menuPlayTile)
        startRound(viewModel.uiState.value.nextArtwork)
    }

    private fun startRound(index: Int) {
        if (!isAdded) return
        val host = activity as? MainActivity ?: return
        host.navigator.showGame(index)
    }

    private fun onGalleryTapped() {
        if (!isAdded || parentFragmentManager.isStateSaved) return
        GemGalleryDialog().show(parentFragmentManager, GemGalleryDialog.TAG)
    }

    private fun onSettingsTapped() {
        if (!isAdded || parentFragmentManager.isStateSaved) return
        SettingsDialog().show(parentFragmentManager, SettingsDialog.TAG)
    }

    override fun onResume() {
        super.onResume()
        viewModel.refresh()
    }

    override fun onDestroyView() {
        val bound = _binding
        PressAnimator.stop(bound?.menuPlayTile)
        bound?.menuArtworkStrip?.adapter = null
        adapter = null
        _binding = null
        super.onDestroyView()
    }
}
