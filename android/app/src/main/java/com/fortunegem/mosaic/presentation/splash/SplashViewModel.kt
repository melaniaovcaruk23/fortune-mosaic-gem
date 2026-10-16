package com.fortunegem.mosaic.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fortunegem.mosaic.core.config.GameConfig
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SplashViewModel : ViewModel() {

    private val readyState = MutableStateFlow(false)
    private val progressState = MutableStateFlow(0)
    private var timerJob: Job? = null

    val handOffReady: StateFlow<Boolean> = readyState.asStateFlow()
    val polishProgress: StateFlow<Int> = progressState.asStateFlow()

    init {
        startTimer()
    }

    private fun startTimer() {
        if (timerJob != null) return
        timerJob = viewModelScope.launch {
            val steps = STEP_COUNT
            val slice = GameConfig.LOADER_DURATION_MS / steps.toLong()
            var step = 0
            while (step < steps) {
                delay(slice)
                step += 1
                progressState.value = step * 100 / steps
            }
            readyState.value = true
        }
    }

    fun consumeHandOff() {
        readyState.value = false
        timerJob = null
    }

    fun captionStage(): Int {
        val value = progressState.value
        return when {
            value < 34 -> STAGE_CUT
            value < 68 -> STAGE_POLISH
            else -> STAGE_SET
        }
    }

    override fun onCleared() {
        timerJob?.cancel()
        timerJob = null
        super.onCleared()
    }

    companion object {
        const val STEP_COUNT = 8
        const val STAGE_CUT = 0
        const val STAGE_POLISH = 1
        const val STAGE_SET = 2
    }
}
