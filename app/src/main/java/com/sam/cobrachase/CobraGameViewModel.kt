package com.sam.cobrachase

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sam.cobrachase.data.CobraGameEvent
import com.sam.cobrachase.data.CobraGameState
import com.sam.cobrachase.data.Coordinate
import com.sam.cobrachase.data.Direction
import com.sam.cobrachase.data.GameState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CobraGameViewModel : ViewModel() {

    private val _state = MutableStateFlow(CobraGameState())
    val state = _state.asStateFlow()

    fun onEvent(event: CobraGameEvent) {
        when (event) {
            is CobraGameEvent.StartGame -> {
                _state.update { it.copy(gameState = GameState.STARTED) }
                viewModelScope.launch {
                    while (state.value.gameState == GameState.STARTED){
                        val delayMillis = when(state.value.cobra.size){
                            in 1 .. 5 -> 120L
                            in 6 .. 10 -> 110L
                            else -> 100L
                        }
                        delay(delayMillis)
                        _state.value = updateGame(state.value)
                    }
                }
            }
            is CobraGameEvent.PauseGame -> {
                _state.update { it.copy(gameState = GameState.PAUSED) }
            }
            is CobraGameEvent.ResetGame -> _state.value = CobraGameState()
            is CobraGameEvent.UpdateDirection -> {
                updateDirection(event.offset, event.canvasWidth)
            }
        }
    }

    private fun updateDirection(offset: Offset, canvasWidth: Int){
        if (!state.value.isGameOver){
            val cellSize = canvasWidth/state.value.xAxisGridSize
            val tapX = (offset.x / cellSize).toInt()
            val tapY = (offset.y / cellSize).toInt()
            val head = state.value.cobra.first()

            _state.update {
                it.copy(
                    direction = when(state.value.direction){
                        Direction.UP, Direction.DOWN->{
                            if ((tapX < head.x)) Direction.LEFT else Direction.RIGHT
                        }
                        Direction.LEFT, Direction.RIGHT -> {
                            if ((tapY < head.y)) Direction.UP else Direction.DOWN
                        }
                    }
                )
            }
        }
    }

    private fun updateGame(currentGame: CobraGameState): CobraGameState{
        if (currentGame.isGameOver)
            return currentGame

        val head = currentGame.cobra.first()
        val xAxisGridSize = currentGame.xAxisGridSize
        val yAxisGridSize = currentGame.yAxisGridSize

        //update the movement of cobra

        val newHead = when(currentGame.direction){
            Direction.UP -> Coordinate(x = head.x, y = head.y-1)
            Direction.DOWN -> Coordinate(x = head.x, y = head.y+1)
            Direction.LEFT -> Coordinate(x = head.x-1, y = head.y)
            Direction.RIGHT -> Coordinate(x = head.x+1, y = head.y)
        }

        //Check if the cobra collide with itself or goes out of bound
        if (currentGame.cobra.contains(newHead) ||
            !isWithinBounds(newHead, xAxisGridSize, yAxisGridSize))
            return currentGame.copy(isGameOver = true)


        //Check if the cobra eats the food
        var newCobra = mutableListOf(newHead) + currentGame.cobra
        val newFood = if (newHead == currentGame.food) CobraGameState.generateRandomFoodCoordinate()
        else currentGame.food

        //Update snake length
        if (newHead != currentGame.food){
            newCobra = newCobra.toMutableList()
            newCobra.removeAt(newCobra.size-1)
        }

        return currentGame.copy(cobra = newCobra, food = newFood)

    }

    private fun isWithinBounds(
        coordinate: Coordinate,
        xAxisGridSize: Int,
        yAxisGridSize: Int
    ): Boolean {
        return coordinate.x in 1 until xAxisGridSize - 1
                && coordinate.y in 1 until yAxisGridSize - 1
    }

}