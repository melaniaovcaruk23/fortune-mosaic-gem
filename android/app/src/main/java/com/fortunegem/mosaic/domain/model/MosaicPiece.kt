package com.fortunegem.mosaic.domain.model

data class MosaicPiece(
    val id: Int,
    val shape: PieceShape,
    val palette: GemPalette
) {
    fun rotate(): MosaicPiece = copy(shape = shape.rotated())
}
