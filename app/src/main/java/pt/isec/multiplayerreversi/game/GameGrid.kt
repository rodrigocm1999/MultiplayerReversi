package pt.isec.multiplayerreversi.game

import android.content.Context
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.get
import androidx.core.view.isVisible
import pt.isec.multiplayerreversi.R
import pt.isec.multiplayerreversi.game.interactors.InteractionProxy
import pt.isec.multiplayerreversi.game.logic.Piece
import pt.isec.multiplayerreversi.game.logic.Vector


class GameGrid(
    context: Context,
    private val gridLayout: GridLayout,
    screenSize: DisplayMetrics,
    layoutInflater: LayoutInflater,
    private val boardSideLength: Int,
    private val interactionProxy: InteractionProxy,
) {

    private val grid: Array<Array<BoardSlotView>>
    private val darkPiece = AppCompatResources.getDrawable(context, R.drawable.piece_dark)

    private val lightPiece = AppCompatResources.getDrawable(context, R.drawable.piece_light)
    private val bluePiece = AppCompatResources.getDrawable(context, R.drawable.piece_blue)
    private val possiblePiece = AppCompatResources.getDrawable(context, R.drawable.piece_possible)
    private var possibleMoves: List<Vector>? = null

    var isUsingBombPiece = false
    var isUsingTrade = false


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
                    when {
                        isUsingBombPiece -> interactionProxy.playBomb(line, column)
                        isUsingTrade -> {
                        }                            // TODO 2 do the trade thing
                        else -> interactionProxy.playAt(line, column)
                    }
                }

                val boardView = BoardSlotView(view, view[0])
                gridLayout.addView(view)
                boardView
            }
        }

        interactionProxy.setUpdateBoardEvent {
            updatePieces(it)
        }
        interactionProxy.setPossibleMovesCallBack {
            showPossibleMoves(it)
        }
    }

    fun clearPossibleMoves(): List<Vector>? {
        possibleMoves?.forEach {
            val boardSlot = grid[it.y][it.x]
            boardSlot.piece.isVisible = false
        }
        return possibleMoves
    }

    fun showPossibleMoves(list: List<Vector>?) {
        if (list == null) return
        for (it in list) {
            val boardSlot = grid[it.y][it.x]
            boardSlot.piece.background = possiblePiece
            boardSlot.piece.isVisible = true
        }
        possibleMoves = list
    }

    private fun updatePieces(board: Array<Array<Piece>>) {
        if (board.size != boardSideLength || board[0].size != boardSideLength)
            throw IllegalStateException("Board is not the same size, should never happen")

        for (line in 0 until boardSideLength) {
            for (column in 0 until boardSideLength) {
                val piece = board[line][column]
                val boardSlotView = grid[line][column].piece

                boardSlotView.background = when (piece) {
                    Piece.Dark -> darkPiece
                    Piece.Light -> lightPiece
                    Piece.Blue -> bluePiece
                    else -> darkPiece
                }
                boardSlotView.isVisible = piece != Piece.Empty
            }
        }
    }

    /*public fun playBombPiece(playerPiece: Piece) {
        for (line in 0 until boardSideLength){
            for (column in 0 until boardSideLength){
                val piece = grid[line][column].piece
                if (piece == playerPiece){

                }
            }
        }
    }*/

    data class BoardSlotView(val slot: ViewGroup, val piece: View)
}