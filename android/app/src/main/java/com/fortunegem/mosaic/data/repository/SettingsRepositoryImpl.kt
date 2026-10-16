package com.fortunegem.mosaic.data.repository

import com.fortunegem.mosaic.data.local.PreferencesStorage
import com.fortunegem.mosaic.domain.repository.SettingsRepository

class SettingsRepositoryImpl(private val storage: PreferencesStorage) : SettingsRepository {

    override fun soundEnabled(): Boolean =
        storage.readBoolean(PreferencesStorage.KEY_SOUND, true)

    override fun setSoundEnabled(enabled: Boolean) {
        storage.writeBoolean(PreferencesStorage.KEY_SOUND, enabled)
    }

    override fun vibrationEnabled(): Boolean =
        storage.readBoolean(PreferencesStorage.KEY_VIBRATION, true)

    override fun setVibrationEnabled(enabled: Boolean) {
        storage.writeBoolean(PreferencesStorage.KEY_VIBRATION, enabled)
    }
}
