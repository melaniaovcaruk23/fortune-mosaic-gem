package com.fortunegem.mosaic.domain.repository

interface ProgressRepository {
    fun bestScore(): Int
    fun submitScore(score: Int): Boolean
    fun unlockedArtwork(): Int
    fun unlockArtwork(index: Int)
    fun starsFor(artworkIndex: Int): Int
    fun saveStars(artworkIndex: Int, stars: Int)
    fun restoredCount(total: Int): Int
    fun onboardingSeen(): Boolean
    fun markOnboardingSeen()
}
