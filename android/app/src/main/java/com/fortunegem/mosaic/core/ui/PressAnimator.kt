package com.fortunegem.mosaic.core.ui

import android.view.View
import android.view.animation.OvershootInterpolator
import com.fortunegem.mosaic.core.config.GameConfig

object PressAnimator {

    fun pulse(view: View?) {
        val target = view ?: return
        try {
            target.animate().cancel()
            target.scaleX = 0.96f
            target.scaleY = 0.96f
            target.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(GameConfig.PRESS_MS)
                .setInterpolator(OvershootInterpolator(2.0f))
                .start()
        } catch (e: Exception) {
            ignore()
        }
    }

    fun stop(view: View?) {
        val target = view ?: return
        try {
            target.animate().cancel()
            target.scaleX = 1f
            target.scaleY = 1f
        } catch (e: Exception) {
            ignore()
        }
    }

    private fun ignore() {
    }
}
