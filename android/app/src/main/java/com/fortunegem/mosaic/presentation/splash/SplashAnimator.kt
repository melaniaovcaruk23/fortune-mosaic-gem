package com.fortunegem.mosaic.presentation.splash

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import com.fortunegem.mosaic.core.config.GameConfig

class SplashAnimator {

    private val running = ArrayList<ValueAnimator>()

    fun breathe(target: View?) {
        val view = target ?: return
        val scaleX = ObjectAnimator.ofFloat(view, View.SCALE_X, 1.0f, 1.06f)
        val scaleY = ObjectAnimator.ofFloat(view, View.SCALE_Y, 1.0f, 1.06f)
        configureLoop(scaleX, GameConfig.BREATH_MS)
        configureLoop(scaleY, GameConfig.BREATH_MS)
        scaleX.start()
        scaleY.start()
        running.add(scaleX)
        running.add(scaleY)
    }

    fun drift(target: View?) {
        val view = target ?: return
        val spin = ObjectAnimator.ofFloat(view, View.ROTATION, 0f, 360f)
        spin.duration = 24000L
        spin.repeatCount = ValueAnimator.INFINITE
        spin.repeatMode = ValueAnimator.RESTART
        spin.interpolator = LinearInterpolator()
        spin.start()
        running.add(spin)
    }

    fun twinkle(target: View?, phaseMs: Long) {
        val view = target ?: return
        val fade = ObjectAnimator.ofFloat(view, View.ALPHA, 0.25f, 0.95f)
        fade.duration = 1400L
        fade.startDelay = phaseMs
        fade.repeatCount = ValueAnimator.INFINITE
        fade.repeatMode = ValueAnimator.REVERSE
        fade.interpolator = AccelerateDecelerateInterpolator()
        fade.start()
        running.add(fade)
        val lift = ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, -6f, 6f)
        lift.duration = 2200L
        lift.startDelay = phaseMs
        lift.repeatCount = ValueAnimator.INFINITE
        lift.repeatMode = ValueAnimator.REVERSE
        lift.interpolator = AccelerateDecelerateInterpolator()
        lift.start()
        running.add(lift)
    }

    fun enterGem(target: View?) {
        val view = target ?: return
        view.alpha = 0f
        view.scaleX = 0.82f
        view.scaleY = 0.82f
        view.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(680L)
            .setInterpolator(DecelerateInterpolator())
            .start()
    }

    fun enterText(target: View?, delayMs: Long, density: Float) {
        val view = target ?: return
        view.alpha = 0f
        view.translationY = 24f * density
        view.animate()
            .alpha(1f)
            .translationY(0f)
            .setStartDelay(delayMs)
            .setDuration(520L)
            .setInterpolator(DecelerateInterpolator())
            .start()
    }

    fun haloPulse(target: View?) {
        val view = target ?: return
        val grow = ObjectAnimator.ofFloat(view, View.SCALE_X, 0.92f, 1.08f)
        val tall = ObjectAnimator.ofFloat(view, View.SCALE_Y, 0.92f, 1.08f)
        val glow = ObjectAnimator.ofFloat(view, View.ALPHA, 0.55f, 1f)
        configureLoop(grow, 3200L)
        configureLoop(tall, 3200L)
        configureLoop(glow, 3200L)
        grow.start()
        tall.start()
        glow.start()
        running.add(grow)
        running.add(tall)
        running.add(glow)
    }

    private fun configureLoop(animator: ObjectAnimator, durationMs: Long) {
        animator.duration = durationMs
        animator.repeatCount = ValueAnimator.INFINITE
        animator.repeatMode = ValueAnimator.REVERSE
        animator.interpolator = AccelerateDecelerateInterpolator()
    }

    fun release(vararg views: View?) {
        running.forEach { animator ->
            try {
                animator.cancel()
            } catch (e: Exception) {
                ignore()
            }
        }
        running.clear()
        views.forEach { view ->
            try {
                view?.animate()?.cancel()
            } catch (e: Exception) {
                ignore()
            }
        }
    }

    private fun ignore() {
    }
}
