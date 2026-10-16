package com.fortunegem.mosaic.core.navigation

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.fortunegem.mosaic.R
import com.fortunegem.mosaic.domain.model.RoundResult
import com.fortunegem.mosaic.presentation.game.GameFragment
import com.fortunegem.mosaic.presentation.gameover.GameOverFragment
import com.fortunegem.mosaic.presentation.menu.MenuFragment
import com.fortunegem.mosaic.presentation.splash.SplashFragment

class Navigator(
    private val fragmentManager: FragmentManager,
    private val containerId: Int
) {

    fun showSplash(): Boolean {
        if (fragmentManager.isStateSaved || fragmentManager.isDestroyed) return false
        fragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
            .replace(containerId, SplashFragment(), TAG_SPLASH)
            .commit()
        return true
    }

    fun showMenu(): Boolean {
        if (fragmentManager.isStateSaved || fragmentManager.isDestroyed) return false
        fragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
            .replace(containerId, MenuFragment(), TAG_MENU)
            .commit()
        return true
    }

    fun showGame(artworkIndex: Int): Boolean {
        if (fragmentManager.isStateSaved || fragmentManager.isDestroyed) return false
        fragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left,
                R.anim.fade_in,
                R.anim.fade_out
            )
            .replace(containerId, GameFragment.newInstance(artworkIndex), TAG_GAME)
            .addToBackStack(TAG_GAME)
            .commit()
        return true
    }

    fun showGameOver(result: RoundResult): Boolean {
        if (fragmentManager.isStateSaved || fragmentManager.isDestroyed) return false
        fragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.fade_in,
                R.anim.fade_out,
                R.anim.fade_in,
                R.anim.fade_out
            )
            .replace(containerId, GameOverFragment.newInstance(result), TAG_RESULT)
            .addToBackStack(TAG_RESULT)
            .commit()
        return true
    }

    fun backToMenu(): Boolean {
        if (fragmentManager.isStateSaved || fragmentManager.isDestroyed) return false
        fragmentManager.popBackStack(TAG_GAME, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        return true
    }

    fun replayGame(artworkIndex: Int): Boolean {
        if (fragmentManager.isStateSaved || fragmentManager.isDestroyed) return false
        fragmentManager.popBackStack(TAG_GAME, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        return showGame(artworkIndex)
    }

    fun currentFragment(): Fragment? = fragmentManager.findFragmentById(containerId)

    companion object {
        const val TAG_SPLASH = "splash"
        const val TAG_MENU = "menu"
        const val TAG_GAME = "game"
        const val TAG_RESULT = "result"
    }
}
