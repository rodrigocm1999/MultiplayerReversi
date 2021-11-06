package pt.isec.multiplayerreversi.game

import android.content.Context
import android.graphics.Color
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import pt.isec.multiplayerreversi.R
import pt.isec.multiplayerreversi.game.gamelogic.Piece


class GameGrid(
    context: Context,
    private val gridLayout: GridLayout,
    screenSize: DisplayMetrics,
    layoutInflater: LayoutInflater,
    private val boardSideLength: Int
) {

    private val grid: Array<Array<View>>

    init {
        gridLayout.columnCount = boardSideLength

        val height = screenSize.heightPixels
        val width = screenSize.widthPixels
        var sideLength = width
        if (height < width) sideLength = height
        sideLength /= boardSideLength

        grid = Array(boardSideLength) { line ->
            Array(boardSideLength) { column ->
                val view = layoutInflater.inflate(R.layout.piece_layout, null)
                view.layoutParams = ViewGroup.LayoutParams(sideLength, sideLength)
                view.setOnClickListener {
                    //TODO
                }
                gridLayout.addView(view)
                view
            }
        }
    }

    fun updateViews(board: Array<Array<Piece>>) { // TODO fazer observavel provavelmente
        if (board.size != boardSideLength || board[0].size != boardSideLength)
            throw IllegalStateException("Board is not the same size, should never happen")

        for (line in 0 until boardSideLength) {
            for (column in 0 until boardSideLength) {
                val piece = board[line][column]
                val view = grid[line][column]

                view.background.alpha = 100
                when (piece) {
                    Piece.Dark -> view.setBackgroundColor(Color.DKGRAY)
                    Piece.Light -> view.setBackgroundColor(Color.LTGRAY)
                    Piece.Blue -> view.setBackgroundColor(Color.BLUE)
                    Piece.Empty -> view.background.alpha = 0
                }
            }
        }
    }

}