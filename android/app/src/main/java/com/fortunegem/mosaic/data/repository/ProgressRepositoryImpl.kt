package com.fortunegem.mosaic.data.repository

import com.fortunegem.mosaic.data.local.PreferencesStorage
import com.fortunegem.mosaic.domain.repository.ProgressRepository

class ProgressRepositoryImpl(private val storage: PreferencesStorage) : ProgressRepository {

    override fun bestScore(): Int = storage.readInt(PreferencesStorage.KEY_BEST_SCORE, 0)

    override fun submitScore(score: Int): Boolean {
        val current = bestScore()
        if (score <= current) return false
        storage.writeInt(PreferencesStorage.KEY_BEST_SCORE, score)
        return true
    }

    override fun unlockedArtwork(): Int = storage.readInt(PreferencesStorage.KEY_UNLOCKED, 0)

    override fun unlockArtwork(index: Int) {
        if (index > unlockedArtwork()) {
            storage.writeInt(PreferencesStorage.KEY_UNLOCKED, index)
        }
    }

    override fun starsFor(artworkIndex: Int): Int =
        storage.readInt(PreferencesStorage.KEY_STARS_PREFIX + artworkIndex, 0)

    override fun saveStars(artworkIndex: Int, stars: Int) {
        if (stars > starsFor(artworkIndex)) {
            storage.writeInt(PreferencesStorage.KEY_STARS_PREFIX + artworkIndex, stars)
        }
    }

    override fun restoredCount(total: Int): Int {
        var restored = 0
        var index = 0
        while (index < total) {
            if (starsFor(index) > 0) restored += 1
            index += 1
        }
        return restored
    }

    override fun onboardingSeen(): Boolean =
        storage.readBoolean(PreferencesStorage.KEY_ONBOARDING, false)

    override fun markOnboardingSeen() {
        storage.writeBoolean(PreferencesStorage.KEY_ONBOARDING, true)
    }
}
