package pt.isec.multiplayerreversi.game

import android.content.Context
import android.graphics.Color
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.core.view.isVisible
import pt.isec.multiplayerreversi.R
import pt.isec.multiplayerreversi.game.interactors.senders.InteractionSenderProxy
import pt.isec.multiplayerreversi.game.logic.Piece
import pt.isec.multiplayerreversi.game.logic.Vector
import pt.isec.multiplayerreversi.TAG


class GameGrid(
    context: Context,
    private val gridLayout: GridLayout,
    screenSize: DisplayMetrics,
    layoutInflater: LayoutInflater,
    private val boardSideLength: Int,
    private val interactionProxy: InteractionSenderProxy
) {

    private val grid: Array<Array<BoardSlotView>>

    private val normalSlotBackground =
        ContextCompat.getColor(context, R.color.light_yellow_board_background)
    private val possiblePlayBackground = Color.YELLOW

    private val darkPiece = AppCompatResources.getDrawable(context, R.drawable.piece_dark)
    private val lightPiece = AppCompatResources.getDrawable(context, R.drawable.piece_light)
    private val bluePiece = AppCompatResources.getDrawable(context, R.drawable.piece_blue)

    private var possibleMoves: List<Vector>? = null

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
                    interactionProxy.playAt(line, column)
                }
                val boardView = BoardSlotView(view, view[0])
                gridLayout.addView(view)
                boardView
            }
        }

        interactionProxy.setUpdateBoardEvent {
            updatePieces(it)
        }
        interactionProxy.setChangePlayerCallback {
            //TODO 4
        }
        interactionProxy.setPossibleMovesCallBack {
            showPossibleMoves(it)
        }
    }

    fun clearPossibleMoves() {
        possibleMoves?.forEach {
            val boardSlot = grid[it.y][it.x]
            boardSlot.piece.setBackgroundColor(normalSlotBackground)
            boardSlot.piece.isVisible = false
        }
    }

    fun showPossibleMoves(list: List<Vector>) {
        clearPossibleMoves()
        for (it in list){
            val boardSlot = grid[it.y][it.x]
            boardSlot.piece.setBackgroundColor(possiblePlayBackground)
            boardSlot.piece.isVisible = true
            Log.i(TAG,"$it")
        }
        possibleMoves = list
    }

    fun updatePieces(board: Array<Array<Piece>>) {
        if (board.size != boardSideLength || board[0].size != boardSideLength)
            throw IllegalStateException("Board is not the same size, should never happen")

        for (line in 0 until boardSideLength) {
            for (column in 0 until boardSideLength) {
                val piece = board[line][column]
                val boardSlotView = grid[line][column].piece

                when (piece) {
                    Piece.Dark -> boardSlotView.background = darkPiece
                    Piece.Light -> boardSlotView.background = lightPiece
                    Piece.Blue -> boardSlotView.background = bluePiece
                }
                boardSlotView.isVisible = piece != Piece.Empty
            }
        }
    }

    data class BoardSlotView(val slot: ViewGroup, val piece: View)
}