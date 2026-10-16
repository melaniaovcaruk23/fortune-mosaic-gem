package com.fortunegem.mosaic.presentation.gameover

import androidx.lifecycle.ViewModel
import com.fortunegem.mosaic.R
import com.fortunegem.mosaic.domain.model.RoundOutcome
import com.fortunegem.mosaic.domain.repository.ArtworkRepository
import com.fortunegem.mosaic.domain.repository.ProgressRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GameOverViewModel(
    private val progress: ProgressRepository,
    private val artworks: ArtworkRepository
) : ViewModel() {

    private val state = MutableStateFlow(GameOverUiState.empty())
    val uiState: StateFlow<GameOverUiState> = state.asStateFlow()

    fun bind(
        outcomeName: String,
        artworkIndex: Int,
        artworkNameRes: Int,
        score: Int,
        sealedCells: Int,
        totalCells: Int,
        stars: Int,
        newBest: Boolean
    ) {
        if (state.value.headlineRes != 0) return
        val outcome = parse(outcomeName)
        if (stars > 0) {
            progress.saveStars(artworkIndex, stars)
            progress.unlockArtwork(artworkIndex + 1)
        }
        val replay = if (outcome == RoundOutcome.RESTORED) {
            val next = artworkIndex + 1
            if (next >= artworks.count()) artworkIndex else next
        } else {
            artworkIndex
        }
        state.value = GameOverUiState(
            headlineRes = headlineFor(outcome),
            headlineColorRes = colourFor(outcome),
            artworkNameRes = artworkNameRes,
            score = score,
            sealedCells = sealedCells,
            totalCells = totalCells,
            stars = stars,
            bestScore = progress.bestScore(),
            newBest = newBest,
            replayIndex = replay
        )
    }

    private fun parse(name: String): RoundOutcome = when (name) {
        RoundOutcome.RESTORED.name -> RoundOutcome.RESTORED
        RoundOutcome.TIME_UP.name -> RoundOutcome.TIME_UP
        else -> RoundOutcome.FLAWED
    }

    private fun headlineFor(outcome: RoundOutcome): Int = when (outcome) {
        RoundOutcome.RESTORED -> R.string.result_restored
        RoundOutcome.FLAWED -> R.string.result_flawed
        RoundOutcome.TIME_UP -> R.string.result_time_up
    }

    private fun colourFor(outcome: RoundOutcome): Int = when (outcome) {
        RoundOutcome.RESTORED -> R.color.primary_violet
        RoundOutcome.FLAWED -> R.color.accent_rose
        RoundOutcome.TIME_UP -> R.color.accent_amber
    }
}
