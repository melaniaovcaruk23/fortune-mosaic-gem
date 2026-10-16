package com.fortunegem.mosaic.domain.model

data class RoundResult(
    val outcome: RoundOutcome,
    val artworkIndex: Int,
    val artworkNameRes: Int,
    val score: Int,
    val sealedCells: Int,
    val totalCells: Int,
    val stars: Int,
    val bestScore: Int,
    val newBest: Boolean,
    val assisted: Boolean
)
