package com.fortunegem.mosaic.presentation.gameover

data class GameOverUiState(
    val headlineRes: Int,
    val headlineColorRes: Int,
    val artworkNameRes: Int,
    val score: Int,
    val sealedCells: Int,
    val totalCells: Int,
    val stars: Int,
    val bestScore: Int,
    val newBest: Boolean,
    val replayIndex: Int
) {
    companion object {
        fun empty(): GameOverUiState = GameOverUiState(
            headlineRes = 0,
            headlineColorRes = 0,
            artworkNameRes = 0,
            score = 0,
            sealedCells = 0,
            totalCells = 0,
            stars = 0,
            bestScore = 0,
            newBest = false,
            replayIndex = 0
        )
    }
}
