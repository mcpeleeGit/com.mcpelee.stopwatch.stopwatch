package com.mcpelee.stopwatch.stopwatch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StopwatchTheme {
                StopwatchScreen()
            }
        }
    }
}

@Composable
private fun StopwatchTheme(content: @Composable () -> Unit) {
    val colorScheme = lightColorScheme(
        primary = Color(0xFF0B57D0),
        onPrimary = Color.White,
        secondary = Color(0xFF3F5F7F),
        background = Color.White,
        surface = Color.White,
        onBackground = Color(0xFF111827),
        onSurface = Color(0xFF111827),
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MaterialTheme.typography,
        content = content,
    )
}
