package com.fortunegem.mosaic.domain.model

data class Artwork(
    val index: Int,
    val nameRes: Int,
    val gemRes: Int,
    val palette: GemPalette,
    val carving: List<List<Int>>,
    val parScore: Int
) {
    val silhouette: Set<Int>
        get() = carving.flatten().toSet()

    val cellCount: Int
        get() = carving.sumOf { it.size }

    val pieceCount: Int
        get() = carving.size
}
