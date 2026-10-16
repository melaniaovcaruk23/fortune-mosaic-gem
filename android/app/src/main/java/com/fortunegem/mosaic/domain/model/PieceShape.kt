package com.fortunegem.mosaic.domain.model

data class PieceShape(val cells: List<Int>) {

    val rows: Int
        get() = (cells.maxOfOrNull { it / STRIDE } ?: -1) + 1

    val cols: Int
        get() = (cells.maxOfOrNull { it % STRIDE } ?: -1) + 1

    val size: Int
        get() = cells.size

    fun rowOf(packed: Int): Int = packed / STRIDE

    fun colOf(packed: Int): Int = packed % STRIDE

    fun rotated(): PieceShape {
        if (cells.isEmpty()) return this
        val maxRow = rows - 1
        val turned = cells.map { packed ->
            pack(packed % STRIDE, maxRow - packed / STRIDE)
        }
        return normalise(turned)
    }

    fun rotatedTimes(turns: Int): PieceShape {
        var shape = this
        var left = ((turns % 4) + 4) % 4
        while (left > 0) {
            shape = shape.rotated()
            left -= 1
        }
        return shape
    }

    companion object {
        const val STRIDE = 8

        fun pack(row: Int, col: Int): Int = row * STRIDE + col

        fun normalise(cells: List<Int>): PieceShape {
            if (cells.isEmpty()) return PieceShape(emptyList())
            val minRow = cells.minOf { it / STRIDE }
            val minCol = cells.minOf { it % STRIDE }
            val shifted = cells
                .map { pack(it / STRIDE - minRow, it % STRIDE - minCol) }
                .sorted()
            return PieceShape(shifted)
        }

        fun fromBoardCells(boardCells: List<Int>, boardSize: Int): PieceShape {
            val packed = boardCells.map { pack(it / boardSize, it % boardSize) }
            return normalise(packed)
        }
    }
}
