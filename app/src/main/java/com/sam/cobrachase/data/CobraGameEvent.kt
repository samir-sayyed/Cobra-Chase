package com.sam.cobrachase.data

import androidx.compose.ui.geometry.Offset

sealed class CobraGameEvent {
    data object StartGame : CobraGameEvent()
    data object PauseGame : CobraGameEvent()
    data object ResetGame : CobraGameEvent()
    data class UpdateDirection(val offset: Offset, val canvasWidth: Int) : CobraGameEvent()
}