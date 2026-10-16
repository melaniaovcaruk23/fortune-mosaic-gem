package com.fortunegem.mosaic.presentation.game

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.fortunegem.mosaic.MainActivity
import com.fortunegem.mosaic.R
import com.fortunegem.mosaic.core.di.ServiceLocator
import com.fortunegem.mosaic.core.di.ViewModelFactory
import com.fortunegem.mosaic.core.ext.ViewExt
import com.fortunegem.mosaic.core.ui.FeedbackController
import com.fortunegem.mosaic.core.ui.PressAnimator
import com.fortunegem.mosaic.core.ui.ScoreFormatter
import com.fortunegem.mosaic.databinding.FragmentGameBinding
import com.fortunegem.mosaic.domain.model.RoundResult
import com.fortunegem.mosaic.presentation.dialog.PauseDialog
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class GameFragment : Fragment() {

    private var _binding: FragmentGameBinding? = null
    private var feedback: FeedbackController? = null
    private var clearFlashTask: Runnable? = null
    private var sweepStarted: Boolean = false
    private var shownScore: CharSequence = ""
    private var shownArtworkRes: Int = 0
    private var shownFlaws: Int = -1

    private val viewModel: GameViewModel by viewModels {
        ViewModelFactory(requireContext().applicationContext)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val created = FragmentGameBinding.inflate(inflater, container, false)
        _binding = created
        return created.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bound = _binding ?: return
        feedback = FeedbackController(ServiceLocator.settingsRepository(requireContext()))
        bound.gameBoard.onCellTapped = { cell -> viewModel.tapCell(cell) }
        bound.gameTray.onPieceSelected = { index -> viewModel.selectPiece(index) }
        bound.gameRotateButton.setOnClickListener {
            PressAnimator.pulse(bound.gameRotateButton)
            viewModel.rotateSelected()
        }
        bound.gamePlaceButton.setOnClickListener {
            PressAnimator.pulse(bound.gamePlaceButton)
            viewModel.placeSelected()
        }
        bound.gamePauseButton.setOnClickListener { openPause() }
        listenForPauseChoice()
        viewModel.start(readArtworkIndex())
        observeState()
        observeResult()
    }

    private fun readArtworkIndex(): Int = arguments?.getInt(ARG_ARTWORK, 0) ?: 0

    private fun listenForPauseChoice() {
        setFragmentResultListener(PauseDialog.REQUEST_KEY) { _, bundle ->
            val choice = bundle.getString(PauseDialog.RESULT_CHOICE)
            if (choice == PauseDialog.CHOICE_MENU) leaveToMenu()
            if (choice == PauseDialog.CHOICE_RESTART) restartRound()
        }
    }

    private fun openPause() {
        if (!isAdded || parentFragmentManager.isStateSaved) return
        PauseDialog().show(parentFragmentManager, PauseDialog.TAG)
    }

    private fun restartRound() {
        if (!isAdded) return
        val host = activity as? MainActivity ?: return
        host.navigator.replayGame(readArtworkIndex())
    }

    private fun leaveToMenu() {
        if (!isAdded) return
        val host = activity as? MainActivity ?: return
        host.navigator.backToMenu()
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collectLatest { state -> render(state) }
            }
        }
    }

    private fun observeResult() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.roundResult.collectLatest { result ->
                    if (result != null) handOff(result)
                }
            }
        }
    }

    private fun handOff(result: RoundResult) {
        if (!isAdded) return
        val host = activity as? MainActivity ?: return
        dismissPause()
        if (!host.navigator.showGameOver(result)) return
        viewModel.consumeResult()
    }

    private fun dismissPause() {
        if (parentFragmentManager.isStateSaved) return
        val open = parentFragmentManager.findFragmentByTag(PauseDialog.TAG) as? DialogFragment
        open?.dismissAllowingStateLoss()
    }

    private fun render(state: GameUiState) {
        val bound = _binding ?: return
        if (state.artworkNameRes != 0 && state.artworkNameRes != shownArtworkRes) {
            shownArtworkRes = state.artworkNameRes
            bound.gameArtworkName.setText(state.artworkNameRes)
        }
        val scoreText = ScoreFormatter.points(state.score)
        if (scoreText != shownScore) {
            shownScore = scoreText
            bound.gameScore.text = scoreText
        }
        bound.gameBoard.render(state.board)
        bound.gameBoard.showGhost(state.ghostCells, state.ghostValid)
        bound.gameTray.render(state.tray, state.selectedIndex)
        bound.gameStatPieces.bind(
            state.piecesLeft.toString(),
            getString(R.string.label_pieces)
        )
        bound.gameStatTime.bind(
            ScoreFormatter.clock(state.timeLeftMs),
            getString(R.string.label_time)
        )
        renderFlaws(state)
        renderNotice(state)
        ViewExt.setEnabledState(bound.gamePlaceButton, state.placeEnabled)
        if (state.phase == GamePhase.INVALID) onInvalid(state)
        if (state.sweeping && !sweepStarted) {
            sweepStarted = true
            bound.gameBoard.playSweep()
        }
    }

    private fun renderFlaws(state: GameUiState) {
        val bound = _binding ?: return
        if (state.flaws <= 0) {
            bound.gameStatFlaws.visibility = View.INVISIBLE
            return
        }
        bound.gameStatFlaws.visibility = View.VISIBLE
        bound.gameStatFlaws.bind(
            getString(R.string.flaw_fraction, state.flaws, 3),
            getString(R.string.label_flaws)
        )
        if (state.flaws != shownFlaws) {
            shownFlaws = state.flaws
            ViewExt.describeState(
                bound.gameStatFlaws,
                getString(R.string.cd_flaws_state, state.flaws, 3)
            )
        }
    }

    private fun renderNotice(state: GameUiState) {
        val bound = _binding ?: return
        if (state.noticeRes == GameUiState.NO_NOTICE) {
            bound.gameNotice.visibility = View.GONE
            return
        }
        bound.gameNoticeText.setText(state.noticeRes)
        bound.gameNotice.visibility = View.VISIBLE
    }

    private fun onInvalid(state: GameUiState) {
        val bound = _binding ?: return
        bound.gameBoard.flash(state.invalidCells)
        feedback?.reject(bound.gameBoard)
        ViewExt.cancel(bound.root, clearFlashTask)
        clearFlashTask = ViewExt.safePost(bound.root, 260L) {
            val inner = _binding ?: return@safePost
            inner.gameBoard.clearFlash()
            feedback?.stop(inner.gameBoard)
        }
    }

    private fun recolourScore() {
        val bound = _binding ?: return
        bound.gameScore.setTextColor(
            ContextCompat.getColor(requireContext(), R.color.primary_violet)
        )
    }

    override fun onResume() {
        super.onResume()
        recolourScore()
    }

    override fun onDestroyView() {
        val bound = _binding
        ViewExt.cancel(bound?.root, clearFlashTask)
        clearFlashTask = null
        bound?.gameBoard?.stopAnimations()
        bound?.gameBoard?.onCellTapped = null
        bound?.gameTray?.onPieceSelected = null
        PressAnimator.stop(bound?.gameRotateButton)
        PressAnimator.stop(bound?.gamePlaceButton)
        feedback?.stop(bound?.gameBoard)
        feedback = null
        sweepStarted = false
        shownScore = ""
        shownArtworkRes = 0
        shownFlaws = -1
        _binding = null
        super.onDestroyView()
    }

    companion object {
        const val ARG_ARTWORK = "artwork_index"

        fun newInstance(artworkIndex: Int): GameFragment {
            val fragment = GameFragment()
            val args = Bundle()
            args.putInt(ARG_ARTWORK, artworkIndex)
            fragment.arguments = args
            return fragment
        }
    }
}
