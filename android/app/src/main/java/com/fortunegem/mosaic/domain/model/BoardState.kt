package com.fortunegem.mosaic.domain.model

data class BoardState(
    val size: Int,
    val silhouette: Set<Int>,
    val filled: Map<Int, GemPalette>
) {
    val sealedCells: Int
        get() = filled.size

    val totalCells: Int
        get() = silhouette.size

    val isComplete: Boolean
        get() = silhouette.all { filled.containsKey(it) }

    val openCells: List<Int>
        get() = silhouette.filter { !filled.containsKey(it) }.sorted()

    fun withCells(cells: List<Int>, palette: GemPalette): BoardState {
        val next = filled.toMutableMap()
        cells.forEach { next[it] = palette }
        return copy(filled = next)
    }

    companion object {
        fun empty(size: Int, silhouette: Set<Int>): BoardState =
            BoardState(size, silhouette, emptyMap())
    }
}
