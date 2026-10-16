package com.fortunegem.mosaic.core.di

import android.content.Context
import com.fortunegem.mosaic.data.local.PreferencesStorage
import com.fortunegem.mosaic.data.repository.ArtworkRepositoryImpl
import com.fortunegem.mosaic.data.repository.ProgressRepositoryImpl
import com.fortunegem.mosaic.data.repository.SettingsRepositoryImpl
import com.fortunegem.mosaic.domain.repository.ArtworkRepository
import com.fortunegem.mosaic.domain.repository.ProgressRepository
import com.fortunegem.mosaic.domain.repository.SettingsRepository
import com.fortunegem.mosaic.domain.usecase.BuildPieceQueueUseCase
import com.fortunegem.mosaic.domain.usecase.ScoreRoundUseCase
import com.fortunegem.mosaic.domain.usecase.ValidatePlacementUseCase

object ServiceLocator {

    private var storageRef: PreferencesStorage? = null
    private var progressRef: ProgressRepository? = null
    private var settingsRef: SettingsRepository? = null
    private var artworkRef: ArtworkRepository? = null

    val buildPieceQueue: BuildPieceQueueUseCase = BuildPieceQueueUseCase()
    val validatePlacement: ValidatePlacementUseCase = ValidatePlacementUseCase()
    val scoreRound: ScoreRoundUseCase = ScoreRoundUseCase()

    fun init(context: Context) {
        if (storageRef != null) return
        val storage = PreferencesStorage(context.applicationContext)
        storageRef = storage
        progressRef = ProgressRepositoryImpl(storage)
        settingsRef = SettingsRepositoryImpl(storage)
        artworkRef = ArtworkRepositoryImpl()
    }

    fun progressRepository(context: Context): ProgressRepository {
        init(context)
        return progressRef ?: ProgressRepositoryImpl(requireStorage(context))
    }

    fun settingsRepository(context: Context): SettingsRepository {
        init(context)
        return settingsRef ?: SettingsRepositoryImpl(requireStorage(context))
    }

    fun artworkRepository(context: Context): ArtworkRepository {
        init(context)
        return artworkRef ?: ArtworkRepositoryImpl()
    }

    private fun requireStorage(context: Context): PreferencesStorage {
        val existing = storageRef
        if (existing != null) return existing
        val created = PreferencesStorage(context.applicationContext)
        storageRef = created
        return created
    }
}
