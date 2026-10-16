package com.fortunegem.mosaic.core.ui

import android.view.HapticFeedbackConstants
import android.view.View
import android.view.animation.CycleInterpolator
import com.fortunegem.mosaic.core.config.GameConfig
import com.fortunegem.mosaic.domain.repository.SettingsRepository

class FeedbackController(private val settings: SettingsRepository) {

    fun confirm(view: View?) {
        val target = view ?: return
        try {
            if (settings.vibrationEnabled()) {
                target.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
            }
            if (settings.soundEnabled()) {
                PressAnimator.pulse(target)
            }
        } catch (e: Exception) {
            ignore()
        }
    }

    fun reject(view: View?) {
        val target = view ?: return
        try {
            if (settings.vibrationEnabled()) {
                target.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            }
            val amplitude = 8f * target.resources.displayMetrics.density
            target.animate().cancel()
            target.translationX = 0f
            target.animate()
                .translationX(amplitude)
                .setDuration(GameConfig.SHAKE_MS)
                .setInterpolator(CycleInterpolator(2f))
                .start()
        } catch (e: Exception) {
            ignore()
        }
    }

    fun stop(view: View?) {
        val target = view ?: return
        try {
            target.animate().cancel()
            target.translationX = 0f
        } catch (e: Exception) {
            ignore()
        }
    }

    private fun ignore() {
    }
}
