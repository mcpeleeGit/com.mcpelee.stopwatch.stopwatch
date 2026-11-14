package com.googsu.stopwatch

data class StopwatchUiState(
    val elapsedMillis: Long = 0L,
    val isRunning: Boolean = false,
    val laps: List<LapTime> = emptyList(),
)

data class LapTime(
    val number: Int,
    val elapsedMillis: Long,
)
