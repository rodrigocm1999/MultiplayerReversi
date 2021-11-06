package pt.isec.multiplayerreversi.game

import android.content.Context
import android.graphics.Color
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import androidx.core.content.ContextCompat
import androidx.core.view.get
import pt.isec.multiplayerreversi.R
import pt.isec.multiplayerreversi.game.logic.Piece
import pt.isec.multiplayerreversi.game.logic.Vector


class GameGrid(
    context: Context,
    private val gridLayout: GridLayout,
    screenSize: DisplayMetrics,
    layoutInflater: LayoutInflater,
    private val boardSideLength: Int
) {

    private val grid: Array<Array<BoardSlotView>>

    private val normalSlotBackground =
        ContextCompat.getColor(context, R.color.light_yellow_board_background)
    private val possiblePlayBackground = Color.YELLOW

    init {
        gridLayout.columnCount = boardSideLength

        val height = screenSize.heightPixels
        val width = screenSize.widthPixels
        var sideLength = width
        if (height < width) sideLength = height
        sideLength /= boardSideLength

        grid = Array(boardSideLength) { line ->
            Array(boardSideLength) { column ->
                val view = layoutInflater.inflate(R.layout.piece_layout, null) as ViewGroup
                view.layoutParams = ViewGroup.LayoutParams(sideLength, sideLength)
                view.setOnClickListener {
                    //TODO
                }
                val boardView = BoardSlotView(view, view[0])
                gridLayout.addView(view)
                boardView
            }
        }
    }

    fun clearPossibleMoves() {
        for (line in 0 until boardSideLength) {
            for (column in 0 until boardSideLength) {
                val boardSlot = grid[line][column]
                boardSlot.slot.setBackgroundColor(normalSlotBackground)
            }
        }
    }

    fun showPossibleMoves(list: List<Vector>) {
        clearPossibleMoves()
        for (pos in list)
            grid[pos.y][pos.x].slot.setBackgroundColor(possiblePlayBackground)
    }

    fun updatePieces(board: Array<Array<Piece>>) { // TODO fazer observavel provavelmente
        if (board.size != boardSideLength || board[0].size != boardSideLength)
            throw IllegalStateException("Board is not the same size, should never happen")

        for (line in 0 until boardSideLength) {
            for (column in 0 until boardSideLength) {
                val piece = board[line][column]
                val boardSlot = grid[line][column]
                val boardSlotView = boardSlot.piece

                boardSlotView.background.alpha = 100
                when (piece) {
                    Piece.Dark -> boardSlotView.setBackgroundColor(Color.DKGRAY)
                    Piece.Light -> boardSlotView.setBackgroundColor(Color.LTGRAY)
                    Piece.Blue -> boardSlotView.setBackgroundColor(Color.BLUE)
                    Piece.Empty -> boardSlotView.background.alpha = 0
                }
            }
        }
    }


    data class BoardSlotView(val slot: ViewGroup, val piece: View) {}
}