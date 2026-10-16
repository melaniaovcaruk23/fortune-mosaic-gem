package com.fortunegem.mosaic.domain.usecase

import com.fortunegem.mosaic.domain.model.Artwork
import com.fortunegem.mosaic.domain.model.GemPalette
import com.fortunegem.mosaic.domain.model.MosaicPiece
import com.fortunegem.mosaic.domain.model.PieceShape

class BuildPieceQueueUseCase {

    operator fun invoke(artwork: Artwork, boardSize: Int): List<MosaicPiece> {
        val shapes = artwork.carving.map { PieceShape.fromBoardCells(it, boardSize) }
        val order = deterministicOrder(shapes.size, artwork.index)
        return order.mapIndexed { position, sourceIndex ->
            val turns = (artwork.index + position * 3) % 4
            MosaicPiece(
                id = position,
                shape = shapes[sourceIndex].rotatedTimes(turns),
                palette = tintFor(artwork.palette, position)
            )
        }
    }

    private fun deterministicOrder(count: Int, seed: Int): List<Int> {
        val pool = (0 until count).toMutableList()
        val ordered = ArrayList<Int>(count)
        var cursor = seed * 7 + 11
        while (pool.isNotEmpty()) {
            cursor = (cursor * 31 + 17) and 0x7FFFFFFF
            val pick = cursor % pool.size
            ordered.add(pool.removeAt(pick))
        }
        return ordered
    }

    private fun tintFor(palette: GemPalette, position: Int): GemPalette {
        val step = position % 3
        if (step == 0) return palette
        val factor = if (step == 1) 1.07f else 0.93f
        return GemPalette(
            base = shift(palette.base, factor),
            light = shift(palette.light, factor),
            dark = shift(palette.dark, factor),
            spark = palette.spark
        )
    }

    private fun shift(color: Int, factor: Float): Int {
        val alpha = (color ushr 24) and 0xFF
        val red = clamp(((color ushr 16) and 0xFF) * factor)
        val green = clamp(((color ushr 8) and 0xFF) * factor)
        val blue = clamp((color and 0xFF) * factor)
        return (alpha shl 24) or (red shl 16) or (green shl 8) or blue
    }

    private fun clamp(value: Float): Int {
        val rounded = value.toInt()
        return when {
            rounded < 0 -> 0
            rounded > 255 -> 255
            else -> rounded
        }
    }
}
