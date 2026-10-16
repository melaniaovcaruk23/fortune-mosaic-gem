package com.fortunegem.mosaic.domain.repository

interface SettingsRepository {
    fun soundEnabled(): Boolean
    fun setSoundEnabled(enabled: Boolean)
    fun vibrationEnabled(): Boolean
    fun setVibrationEnabled(enabled: Boolean)
}
