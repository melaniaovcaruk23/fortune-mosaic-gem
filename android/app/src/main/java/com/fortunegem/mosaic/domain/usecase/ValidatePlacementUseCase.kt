package com.fortunegem.mosaic.domain.usecase

import com.fortunegem.mosaic.domain.model.BoardState
import com.fortunegem.mosaic.domain.model.PieceShape

class ValidatePlacementUseCase {

    fun cellsFor(board: BoardState, shape: PieceShape, anchor: Int): List<Int> {
        if (anchor < 0) return emptyList()
        val anchorRow = anchor / board.size
        val anchorCol = anchor % board.size
        return shape.cells.map { packed ->
            val row = anchorRow + shape.rowOf(packed)
            val col = anchorCol + shape.colOf(packed)
            if (col < 0 || col >= board.size || row < 0 || row >= board.size) {
                OUT_OF_BOARD
            } else {
                row * board.size + col
            }
        }
    }

    fun isValid(board: BoardState, shape: PieceShape, anchor: Int): Boolean {
        if (shape.cells.isEmpty()) return false
        val cells = cellsFor(board, shape, anchor)
        if (cells.isEmpty()) return false
        return cells.all { cell ->
            cell != OUT_OF_BOARD &&
                board.silhouette.contains(cell) &&
                !board.filled.containsKey(cell)
        }
    }

    fun firstValidAnchor(board: BoardState, shape: PieceShape): Int {
        if (shape.cells.isEmpty()) return NO_ANCHOR
        val total = board.size * board.size
        var anchor = 0
        while (anchor < total) {
            if (isValid(board, shape, anchor)) return anchor
            anchor += 1
        }
        return NO_ANCHOR
    }

    fun firstValidAnchorWithTurns(board: BoardState, shape: PieceShape): Pair<Int, Int> {
        var turns = 0
        var candidate = shape
        while (turns < 4) {
            val anchor = firstValidAnchor(board, candidate)
            if (anchor != NO_ANCHOR) return Pair(anchor, turns)
            candidate = candidate.rotated()
            turns += 1
        }
        return Pair(NO_ANCHOR, 0)
    }

    companion object {
        const val NO_ANCHOR = -1
        const val OUT_OF_BOARD = -1
    }
}
