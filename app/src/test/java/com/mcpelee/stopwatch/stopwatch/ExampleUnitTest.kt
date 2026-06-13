package com.mcpelee.stopwatch.stopwatch

import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun formatStopwatchTime_underOneHour_usesMinutesSecondsAndCentiseconds() {
        assertEquals("00:00.00", formatStopwatchTime(0L))
        assertEquals("01:02.34", formatStopwatchTime(62_340L))
    }

    @Test
    fun formatStopwatchTime_overOneHour_includesHours() {
        assertEquals("01:00:00.00", formatStopwatchTime(3_600_000L))
        assertEquals("02:03:04.56", formatStopwatchTime(7_384_560L))
    }
}
