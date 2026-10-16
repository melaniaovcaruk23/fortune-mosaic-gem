package com.fortunegem.mosaic.domain.usecase

import com.fortunegem.mosaic.core.config.GameConfig
import com.fortunegem.mosaic.domain.model.Artwork
import com.fortunegem.mosaic.domain.model.BoardState
import com.fortunegem.mosaic.domain.model.RoundOutcome

class ScoreRoundUseCase {

    fun score(
        board: BoardState,
        piecesSeated: Int,
        firstTryPlacements: Int,
        flaws: Int,
        secondsLeft: Int,
        assisted: Boolean
    ): Int {
        var total = piecesSeated * GameConfig.SCORE_PER_PIECE
        total += firstTryPlacements * GameConfig.SCORE_FIRST_TRY
        total += secondsLeft * GameConfig.SCORE_PER_SECOND
        total -= flaws * GameConfig.SCORE_FLAW_PENALTY
        if (board.isComplete) {
            total += board.totalCells * 2
        }
        if (assisted) {
            total = (total * GameConfig.ASSIST_SCORE_FACTOR).toInt()
        }
        return if (total < 0) 0 else total
    }

    fun stars(artwork: Artwork, outcome: RoundOutcome, score: Int): Int {
        if (outcome != RoundOutcome.RESTORED) return 0
        val par = if (artwork.parScore > 0) artwork.parScore else 1
        val ratio = score.toFloat() / par.toFloat()
        return when {
            ratio >= GameConfig.STAR_THREE_RATIO -> 3
            ratio >= GameConfig.STAR_TWO_RATIO -> 2
            else -> 1
        }
    }
}
