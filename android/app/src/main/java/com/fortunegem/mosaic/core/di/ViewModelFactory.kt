package com.fortunegem.mosaic.core.di

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.fortunegem.mosaic.presentation.game.GameViewModel
import com.fortunegem.mosaic.presentation.gameover.GameOverViewModel
import com.fortunegem.mosaic.presentation.menu.MenuViewModel
import com.fortunegem.mosaic.presentation.splash.SplashViewModel

class ViewModelFactory(context: Context) : ViewModelProvider.Factory {

    private val appContext: Context = context.applicationContext

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val created: ViewModel = when {
            modelClass.isAssignableFrom(SplashViewModel::class.java) -> SplashViewModel()
            modelClass.isAssignableFrom(MenuViewModel::class.java) -> MenuViewModel(
                ServiceLocator.progressRepository(appContext),
                ServiceLocator.artworkRepository(appContext)
            )
            modelClass.isAssignableFrom(GameViewModel::class.java) -> GameViewModel(
                ServiceLocator.artworkRepository(appContext),
                ServiceLocator.progressRepository(appContext),
                ServiceLocator.buildPieceQueue,
                ServiceLocator.validatePlacement,
                ServiceLocator.scoreRound
            )
            modelClass.isAssignableFrom(GameOverViewModel::class.java) -> GameOverViewModel(
                ServiceLocator.progressRepository(appContext),
                ServiceLocator.artworkRepository(appContext)
            )
            else -> throw IllegalArgumentException("Unknown ViewModel " + modelClass.name)
        }
        return modelClass.cast(created) ?: throw IllegalStateException("Cast failed")
    }
}
