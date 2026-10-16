package com.fortunegem.mosaic.data.repository

import com.fortunegem.mosaic.data.sample.SampleData
import com.fortunegem.mosaic.domain.model.Artwork
import com.fortunegem.mosaic.domain.repository.ArtworkRepository

class ArtworkRepositoryImpl : ArtworkRepository {

    private val items: List<Artwork> = SampleData.artworks

    override fun artworks(): List<Artwork> = items

    override fun artworkAt(index: Int): Artwork {
        if (items.isEmpty()) throw IllegalStateException("No artworks available")
        val safe = when {
            index < 0 -> 0
            index >= items.size -> 0
            else -> index
        }
        return items[safe]
    }

    override fun count(): Int = items.size
}
