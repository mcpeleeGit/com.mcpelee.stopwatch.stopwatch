package com.mcpelee.stopwatch.stopwatch

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class StopwatchViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(StopwatchUiState())
    val uiState: StateFlow<StopwatchUiState> = _uiState.asStateFlow()

    private var accumulatedElapsedMillis = 0L
    private var startedAtRealtimeMillis = 0L
    private var tickerJob: Job? = null

    fun start() {
        if (_uiState.value.isRunning) return
        startedAtRealtimeMillis = SystemClock.elapsedRealtime()
        _uiState.update { it.copy(isRunning = true) }
        startTicker()
    }

    fun pause() {
        if (!_uiState.value.isRunning) return
        accumulatedElapsedMillis = currentElapsedMillis()
        tickerJob?.cancel()
        _uiState.update {
            it.copy(
                elapsedMillis = accumulatedElapsedMillis,
                isRunning = false,
            )
        }
    }

    fun reset() {
        tickerJob?.cancel()
        accumulatedElapsedMillis = 0L
        startedAtRealtimeMillis = 0L
        _uiState.value = StopwatchUiState()
    }

    fun addLap() {
        val elapsedMillis = currentElapsedMillis()
        if (elapsedMillis <= 0L) return

        _uiState.update { state ->
            val lap = LapTime(
                number = state.laps.size + 1,
                elapsedMillis = elapsedMillis,
            )
            state.copy(laps = listOf(lap) + state.laps)
        }
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = viewModelScope.launch {
            while (true) {
                _uiState.update { it.copy(elapsedMillis = currentElapsedMillis()) }
                delay(TICK_INTERVAL_MILLIS)
            }
        }
    }

    private fun currentElapsedMillis(): Long {
        // elapsedRealtime keeps the stopwatch honest across sleep and background time.
        return if (_uiState.value.isRunning) {
            accumulatedElapsedMillis + (SystemClock.elapsedRealtime() - startedAtRealtimeMillis)
        } else {
            accumulatedElapsedMillis
        }
    }

    override fun onCleared() {
        tickerJob?.cancel()
        super.onCleared()
    }

    private companion object {
        const val TICK_INTERVAL_MILLIS = 30L
    }
}
