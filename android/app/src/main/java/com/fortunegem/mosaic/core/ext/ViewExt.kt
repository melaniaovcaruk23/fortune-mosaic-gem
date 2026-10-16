package com.fortunegem.mosaic.core.ext

import android.view.View
import androidx.core.view.ViewCompat

object ViewExt {

    fun safePost(view: View?, delayMs: Long, action: () -> Unit): Runnable? {
        val target = view ?: return null
        val runnable = Runnable {
            try {
                if (target.isAttachedToWindow) action()
            } catch (e: Exception) {
                clearError()
            }
        }
        target.postDelayed(runnable, delayMs)
        return runnable
    }

    fun cancel(view: View?, runnable: Runnable?) {
        if (view == null || runnable == null) return
        try {
            view.removeCallbacks(runnable)
        } catch (e: Exception) {
            clearError()
        }
    }

    fun describeState(view: View?, state: CharSequence) {
        val target = view ?: return
        try {
            ViewCompat.setStateDescription(target, state)
        } catch (e: Exception) {
            clearError()
        }
    }

    fun enterStaggered(view: View?, position: Int, stepMs: Long) {
        val target = view ?: return
        try {
            target.alpha = 0f
            target.translationY = 18f * target.resources.displayMetrics.density
            target.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(position * stepMs)
                .setDuration(260L)
                .start()
        } catch (e: Exception) {
            clearError()
        }
    }

    fun setEnabledState(view: View?, enabled: Boolean) {
        val target = view ?: return
        target.isEnabled = enabled
        target.alpha = if (enabled) 1f else 0.45f
    }

    private fun clearError() {
    }
}
