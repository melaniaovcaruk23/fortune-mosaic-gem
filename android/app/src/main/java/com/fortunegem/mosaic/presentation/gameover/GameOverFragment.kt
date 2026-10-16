package com.fortunegem.mosaic.presentation.gameover

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.fortunegem.mosaic.MainActivity
import com.fortunegem.mosaic.R
import com.fortunegem.mosaic.core.di.ViewModelFactory
import com.fortunegem.mosaic.core.ext.ViewExt
import com.fortunegem.mosaic.core.ui.PressAnimator
import com.fortunegem.mosaic.core.ui.ScoreFormatter
import com.fortunegem.mosaic.databinding.FragmentGameOverBinding
import com.fortunegem.mosaic.domain.model.RoundResult
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class GameOverFragment : Fragment() {

    private var _binding: FragmentGameOverBinding? = null

    private val viewModel: GameOverViewModel by viewModels {
        ViewModelFactory(requireContext().applicationContext)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val created = FragmentGameOverBinding.inflate(inflater, container, false)
        _binding = created
        return created.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bound = _binding ?: return
        bound.resultAgainButton.setOnClickListener {
            PressAnimator.pulse(bound.resultAgainTile)
            replay()
        }
        bound.resultMenuButton.setOnClickListener {
            PressAnimator.pulse(bound.resultMenuButton)
            backToMenu()
        }
        bindArguments()
        enterMedal()
        observeState()
    }

    private fun bindArguments() {
        val args = arguments ?: return
        viewModel.bind(
            args.getString(ARG_OUTCOME, "") ?: "",
            args.getInt(ARG_INDEX, 0),
            args.getInt(ARG_NAME_RES, 0),
            args.getInt(ARG_SCORE, 0),
            args.getInt(ARG_SEALED, 0),
            args.getInt(ARG_TOTAL, 0),
            args.getInt(ARG_STARS, 0),
            args.getBoolean(ARG_NEW_BEST, false)
        )
    }

    private fun enterMedal() {
        val bound = _binding ?: return
        bound.resultMedalCard.alpha = 0f
        bound.resultMedalCard.scaleX = 0.7f
        bound.resultMedalCard.scaleY = 0.7f
        bound.resultMedalCard.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(520L)
            .setInterpolator(OvershootInterpolator(1.4f))
            .start()
        ViewExt.enterStaggered(bound.resultHeadline, 1, 70L)
        ViewExt.enterStaggered(bound.resultSubline, 2, 70L)
        ViewExt.enterStaggered(bound.resultStars, 3, 70L)
        ViewExt.enterStaggered(bound.resultStatRow, 4, 70L)
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collectLatest { state -> render(state) }
            }
        }
    }

    private fun render(state: GameOverUiState) {
        val bound = _binding ?: return
        if (state.headlineRes == 0) return
        bound.resultHeadline.setText(state.headlineRes)
        bound.resultHeadline.setTextColor(
            ContextCompat.getColor(requireContext(), state.headlineColorRes)
        )
        val artworkName = if (state.artworkNameRes != 0) {
            getString(state.artworkNameRes)
        } else {
            getString(R.string.brand_subtitle)
        }
        bound.resultSubline.text = artworkName + "  " +
            getString(R.string.facets_sealed, state.sealedCells, state.totalCells)
        renderStars(state.stars)
        renderStats(state)
    }

    private fun renderStars(stars: Int) {
        val bound = _binding ?: return
        val earned = ContextCompat.getColor(requireContext(), R.color.accent_amber)
        val missing = ContextCompat.getColor(requireContext(), R.color.star_empty)
        bound.resultStarOne.setColorFilter(if (stars >= 1) earned else missing)
        bound.resultStarTwo.setColorFilter(if (stars >= 2) earned else missing)
        bound.resultStarThree.setColorFilter(if (stars >= 3) earned else missing)
        bound.resultStarOne.contentDescription = getString(R.string.cd_stars, stars)
    }

    private fun renderStats(state: GameOverUiState) {
        val bound = _binding ?: return
        var visible = 0
        if (state.score > 0) {
            bound.resultStatScore.visibility = View.VISIBLE
            bound.resultStatScore.bind(
                ScoreFormatter.points(state.score),
                getString(R.string.label_score)
            )
            visible += 1
        } else {
            bound.resultStatScore.visibility = View.GONE
        }
        if (state.sealedCells > 0) {
            bound.resultStatSealed.visibility = View.VISIBLE
            bound.resultStatSealed.bind(
                getString(R.string.progress_fraction, state.sealedCells, state.totalCells),
                getString(R.string.label_sealed)
            )
            visible += 1
        } else {
            bound.resultStatSealed.visibility = View.GONE
        }
        if (state.bestScore > 0) {
            bound.resultStatBest.visibility = View.VISIBLE
            val value = if (state.newBest) {
                getString(R.string.new_best)
            } else {
                ScoreFormatter.points(state.bestScore)
            }
            bound.resultStatBest.bind(value, getString(R.string.label_best))
            visible += 1
        } else {
            bound.resultStatBest.visibility = View.GONE
        }
        bound.resultStatRow.visibility = if (visible >= 2) View.VISIBLE else View.GONE
    }

    private fun replay() {
        if (!isAdded) return
        val host = activity as? MainActivity ?: return
        host.navigator.replayGame(viewModel.uiState.value.replayIndex)
    }

    private fun backToMenu() {
        if (!isAdded) return
        val host = activity as? MainActivity ?: return
        host.navigator.backToMenu()
    }

    override fun onDestroyView() {
        val bound = _binding
        PressAnimator.stop(bound?.resultAgainTile)
        PressAnimator.stop(bound?.resultMenuButton)
        bound?.resultMedalCard?.animate()?.cancel()
        _binding = null
        super.onDestroyView()
    }

    companion object {
        const val ARG_OUTCOME = "outcome"
        const val ARG_INDEX = "artwork_index"
        const val ARG_NAME_RES = "artwork_name"
        const val ARG_SCORE = "score"
        const val ARG_SEALED = "sealed_cells"
        const val ARG_TOTAL = "total_cells"
        const val ARG_STARS = "stars"
        const val ARG_NEW_BEST = "new_best"

        fun newInstance(result: RoundResult): GameOverFragment {
            val fragment = GameOverFragment()
            val args = Bundle()
            args.putString(ARG_OUTCOME, result.outcome.name)
            args.putInt(ARG_INDEX, result.artworkIndex)
            args.putInt(ARG_NAME_RES, result.artworkNameRes)
            args.putInt(ARG_SCORE, result.score)
            args.putInt(ARG_SEALED, result.sealedCells)
            args.putInt(ARG_TOTAL, result.totalCells)
            args.putInt(ARG_STARS, result.stars)
            args.putBoolean(ARG_NEW_BEST, result.newBest)
            fragment.arguments = args
            return fragment
        }
    }
}
