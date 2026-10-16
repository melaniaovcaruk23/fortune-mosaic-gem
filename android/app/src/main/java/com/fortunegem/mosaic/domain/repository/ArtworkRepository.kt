package com.fortunegem.mosaic.domain.repository

import com.fortunegem.mosaic.domain.model.Artwork

interface ArtworkRepository {
    fun artworks(): List<Artwork>
    fun artworkAt(index: Int): Artwork
    fun count(): Int
}
