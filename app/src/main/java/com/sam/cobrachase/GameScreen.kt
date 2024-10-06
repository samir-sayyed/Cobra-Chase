package com.sam.cobrachase

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.sam.cobrachase.data.CobraGameEvent
import com.sam.cobrachase.data.CobraGameState
import com.sam.cobrachase.data.Coordinate
import com.sam.cobrachase.data.Direction
import com.sam.cobrachase.data.GameState
import com.sam.cobrachase.ui.theme.Citrine
import com.sam.cobrachase.ui.theme.Custard
import com.sam.cobrachase.ui.theme.RoyalBlue

@Composable
fun GameScreen(
    state: CobraGameState,
    onEvent: (CobraGameEvent) -> Unit
) {
    val foodImage = ImageBitmap.imageResource(id = R.drawable.img_apple)
    val cobraImageHead = when(state.direction){
        Direction.RIGHT -> ImageBitmap.imageResource(id = R.drawable.img_snake_head)
        Direction.LEFT -> ImageBitmap.imageResource(id = R.drawable.img_snake_head2)
        Direction.UP -> ImageBitmap.imageResource(id = R.drawable.img_snake_head3)
        Direction.DOWN -> ImageBitmap.imageResource(id = R.drawable.img_snake_head4)
    }

    Box (
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ){
        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceAround
        ) {
            Card(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Score: ${state.cobra.size-1}",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.headlineMedium
                )
            }
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(ratio = 2 / 3f)
                    .pointerInput(state.gameState){
                        if (state.gameState != GameState.STARTED){
                            return@pointerInput
                        }
                        detectTapGestures {
                            onEvent(CobraGameEvent.UpdateDirection(offset = it, size.width))
                        }
                    }
            ) {
                val cellSize = size.width / 20
                drawGameBoard(
                    cellSize = cellSize,
                    cellColor = Custard,
                    borderCellColor = RoyalBlue,
                    gridWidth = state.xAxisGridSize,
                    gridHeight = state.yAxisGridSize
                )
                drawFood(
                    foodImage = foodImage,
                    cellSize = cellSize.toInt(),
                    coordinate = state.food
                )
                drawCobra(
                    cobraImageHead = cobraImageHead,
                    cellSize = cellSize,
                    cobra = state.cobra
                )
            }

            Row(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
            ) {

                Button(
                    onClick = { onEvent(CobraGameEvent.ResetGame) },
                    modifier = Modifier.weight(1f),
                    enabled = state.gameState == GameState.PAUSED || state.isGameOver
                ) {
                    Text(text = if (state.isGameOver)"Reset" else "New Game")
                }

                Spacer(modifier = Modifier.padding(10.dp))

                Button(
                    onClick = {
                        when(state.gameState){
                            GameState.IDLE , GameState.PAUSED-> onEvent(CobraGameEvent.StartGame)
                            GameState.STARTED -> onEvent(CobraGameEvent.PauseGame)
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
                {
                    Text(text = when(state.gameState){
                        GameState.IDLE -> "Start"
                        GameState.STARTED -> "Pause"
                        GameState.PAUSED -> "Resume"
                    })
                }

            }
        }

        AnimatedVisibility(visible = state.isGameOver) {
            Text(
                text = "Game Over",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.displayMedium
            )
        }

    }
}

private fun DrawScope.drawGameBoard(
    cellSize: Float,
    cellColor: Color,
    borderCellColor: Color,
    gridWidth: Int,
    gridHeight: Int
) {
    for (i in 0 until gridWidth) {
        for (j in 0 until gridHeight) {
            val isBorderCell = i == 0 || j == 0 || i == gridWidth - 1 || j == gridHeight - 1
            drawRect(
                color = if (isBorderCell) borderCellColor
                else if ((i + j) % 2 == 0)
                    cellColor
                else
                    cellColor.copy(alpha = 0.5f),
                topLeft = Offset(i * cellSize, j * cellSize),
                size = Size(cellSize, cellSize)
            )
        }
    }
}

private fun DrawScope.drawFood(
    foodImage: ImageBitmap,
    cellSize: Int,
    coordinate: Coordinate
) {
    drawImage(
        image = foodImage,
        dstOffset = IntOffset(
            x = (coordinate.x * cellSize),
            y = (coordinate.y * cellSize)
        ),
        dstSize = IntSize(cellSize, cellSize)
    )
}

private fun DrawScope.drawCobra(
    cobraImageHead : ImageBitmap,
    cellSize: Float,
    cobra: List<Coordinate>
){
    val cellSizeInt = cellSize.toInt()
    cobra.forEachIndexed { index, coordinate ->
        val radius = if (index == cobra.lastIndex) cellSize/2.5f else cellSize/2f
        if (index == 0){
            drawImage(
                image = cobraImageHead
                , dstOffset = IntOffset(
                    x = (coordinate.x * cellSizeInt),
                    y = (coordinate.y * cellSizeInt)
                ),
                dstSize = IntSize(cellSizeInt, cellSizeInt)
                )
        } else {
            drawCircle(
                color = Citrine,
                radius = radius,
                center = Offset(
                    x = (coordinate.x * cellSizeInt) + radius,
                    y = (coordinate.y * cellSizeInt) + radius
                )
            )
        }
    }

}
