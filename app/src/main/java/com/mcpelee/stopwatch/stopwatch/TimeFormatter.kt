package com.mcpelee.stopwatch.stopwatch

fun formatStopwatchTime(elapsedMillis: Long): String {
    val totalCentiseconds = elapsedMillis.coerceAtLeast(0L) / 10L
    val centiseconds = totalCentiseconds % 100L
    val totalSeconds = totalCentiseconds / 100L
    val seconds = totalSeconds % 60L
    val totalMinutes = totalSeconds / 60L
    val minutes = totalMinutes % 60L
    val hours = totalMinutes / 60L

    return if (hours > 0L) {
        "%02d:%02d:%02d.%02d".format(hours, minutes, seconds, centiseconds)
    } else {
        "%02d:%02d.%02d".format(minutes, seconds, centiseconds)
    }
}
