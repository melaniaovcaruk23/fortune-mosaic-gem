package com.fortunegem.mosaic.presentation.game

import com.fortunegem.mosaic.core.config.GameConfig
import com.fortunegem.mosaic.domain.model.BoardState
import com.fortunegem.mosaic.domain.model.MosaicPiece

data class GameUiState(
    val phase: GamePhase,
    val board: BoardState,
    val tray: List<MosaicPiece>,
    val selectedIndex: Int,
    val ghostCells: List<Int>,
    val ghostValid: Boolean,
    val invalidCells: List<Int>,
    val score: Int,
    val flaws: Int,
    val piecesLeft: Int,
    val timeLeftMs: Long,
    val artworkNameRes: Int,
    val noticeRes: Int,
    val placeEnabled: Boolean,
    val sweeping: Boolean
) {
    companion object {
        const val NO_NOTICE = 0

        fun loading(): GameUiState = GameUiState(
            phase = GamePhase.LOADING,
            board = BoardState.empty(GameConfig.BOARD_SIZE, emptySet()),
            tray = emptyList(),
            selectedIndex = 0,
            ghostCells = emptyList(),
            ghostValid = true,
            invalidCells = emptyList(),
            score = 0,
            flaws = 0,
            piecesLeft = 0,
            timeLeftMs = GameConfig.ARTWORK_TIME_MS,
            artworkNameRes = 0,
            noticeRes = NO_NOTICE,
            placeEnabled = false,
            sweeping = false
        )
    }
}
