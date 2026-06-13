package com.mcpelee.stopwatch.stopwatch

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun StopwatchScreen(
    viewModel: StopwatchViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    StopwatchContent(
        uiState = uiState,
        onStartPause = {
            if (uiState.isRunning) viewModel.pause() else viewModel.start()
        },
        onReset = viewModel::reset,
        onLap = viewModel::addLap,
    )
}

@Composable
private fun StopwatchContent(
    uiState: StopwatchUiState,
    onStartPause: () -> Unit,
    onReset: () -> Unit,
    onLap: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 22.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.46f),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = formatStopwatchTime(uiState.elapsedMillis),
                color = MaterialTheme.colorScheme.onBackground,
                fontFamily = FontFamily.Monospace,
                fontSize = if (uiState.elapsedMillis >= ONE_HOUR_MILLIS) 54.sp else 66.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.sp,
                textAlign = TextAlign.Center,
                maxLines = 1,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        LapList(
            laps = uiState.laps,
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.54f),
        )

        Controls(
            isRunning = uiState.isRunning,
            hasElapsedTime = uiState.elapsedMillis > 0L,
            onStartPause = onStartPause,
            onReset = onReset,
            onLap = onLap,
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(top = 18.dp, bottom = 18.dp),
        )
    }
}

@Composable
private fun LapList(
    laps: List<LapTime>,
    modifier: Modifier = Modifier,
) {
    if (laps.isEmpty()) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.TopCenter,
        ) {}
        return
    }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(vertical = 4.dp),
    ) {
        items(
            items = laps,
            key = { it.number },
        ) { lap ->
            LapRow(lap)
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
        }
    }
}

@Composable
private fun LapRow(lap: LapTime) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "랩 ${lap.number}",
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.sp,
        )
        Text(
            text = formatStopwatchTime(lap.elapsedMillis),
            color = MaterialTheme.colorScheme.onSurface,
            fontFamily = FontFamily.Monospace,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.sp,
        )
    }
}

@Composable
private fun Controls(
    isRunning: Boolean,
    hasElapsedTime: Boolean,
    onStartPause: () -> Unit,
    onReset: () -> Unit,
    onLap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedButton(
                onClick = onReset,
                enabled = hasElapsedTime || isRunning,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .sizeIn(minHeight = 58.dp),
            ) {
                Text(text = "초기화", fontSize = 17.sp, letterSpacing = 0.sp)
            }

            OutlinedButton(
                onClick = onLap,
                enabled = hasElapsedTime || isRunning,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .sizeIn(minHeight = 58.dp),
            ) {
                Text(text = "랩", fontSize = 17.sp, letterSpacing = 0.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onStartPause,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isRunning) {
                    MaterialTheme.colorScheme.secondary
                } else {
                    MaterialTheme.colorScheme.primary
                },
            ),
            modifier = Modifier
                .fillMaxWidth()
                .sizeIn(minHeight = 66.dp),
        ) {
            val label = if (isRunning) {
                "일시정지"
            } else if (hasElapsedTime) {
                "재개"
            } else {
                "시작"
            }
            Text(
                text = label,
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.sp,
            )
        }
    }
}

private const val ONE_HOUR_MILLIS = 60L * 60L * 1000L
