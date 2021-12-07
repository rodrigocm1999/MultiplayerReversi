package pt.isec.multiplayerreversi.game

import android.app.Activity
import android.graphics.Color
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.TextView
import androidx.core.view.get
import androidx.core.view.isVisible
import pt.isec.multiplayerreversi.R
import pt.isec.multiplayerreversi.game.interactors.GamePlayer
import pt.isec.multiplayerreversi.game.logic.Piece
import pt.isec.multiplayerreversi.game.logic.Vector


class GameGrid(
    private val context: Activity,
    private val gridLayout: GridLayout,
    screenSize: DisplayMetrics,
    layoutInflater: LayoutInflater,
    private val boardSideLength: Int,
    private val gamePlayer: GamePlayer,
) {

    private val grid: Array<Array<BoardSlotView>>
    private val tradePieces = ArrayList<BoardSlotView>(3)

    var isUsingBombPiece = false
    var isUsingTrade = false
        set(value) {
            field = value
            if (!value)
                clearTrade()
        }

    init {
        gridLayout.columnCount = boardSideLength

        val height = screenSize.heightPixels
        val width = screenSize.widthPixels
        var sideLength = width
        if (height < width) sideLength = height
        sideLength /= boardSideLength

        val start = System.currentTimeMillis()

        grid = Array(boardSideLength) { line ->
            Array(boardSideLength) { column ->
                val view = layoutInflater.inflate(R.layout.piece_layout, null) as ViewGroup
                view.layoutParams = ViewGroup.LayoutParams(sideLength, sideLength)
                val boardView =
                    BoardSlotView(view, view[0], view[1] as TextView, Vector(column, line))

                view.setOnClickListener {
                    when {
                        isUsingBombPiece -> gamePlayer.playBomb(line, column)
                        isUsingTrade -> {
                            addPieceToTrade(boardView)
                            if (tradePieces.size == 3) {
                                val pieces = ArrayList<Vector>(3)
                                tradePieces.forEach { pieces.add(it.vector) }
                                gamePlayer.playTrade(pieces)
                                clearTrade()
                            }
                        }
                        else -> gamePlayer.playAt(line, column)
                    }
                }

                gridLayout.addView(view)
                boardView
            }
        }

        val end = System.currentTimeMillis()
        println("Time taken creating board pieces : ${end - start}ms")
    }

    fun clearPossibleMoves() {
        gamePlayer.getPossibleMoves().forEach {
            val boardSlot = grid[it.y][it.x]
            boardSlot.piece.isVisible = false
        }
    }

    private fun clearTrade() {
        tradePieces.forEach {
            it.slot.setBackgroundResource(R.drawable.square_border)
        }
        tradePieces.clear()
    }

    fun showPossibleMoves() {
        val possibleMoves = gamePlayer.getPossibleMoves()
        val temp = gamePlayer.getCurrentPlayer().piece.getPossibleDrawable(context)
        for (it in possibleMoves) {
            val boardSlot = grid[it.y][it.x]
            boardSlot.piece.background = temp
            boardSlot.piece.isVisible = true
        }
    }

    fun updatePieces() {
        val board = gamePlayer.getGameBoard()
        if (board.size != boardSideLength || board[0].size != boardSideLength)
            throw IllegalStateException("Board is not the same size, should never happen")

        for (line in 0 until boardSideLength) {
            for (column in 0 until boardSideLength) {
                val piece = board[line][column]
                val boardView = grid[line][column]

                boardView.piece.background = piece.getDrawable(context)
                boardView.pieceText.setTextColor(if (piece == Piece.Dark) Color.WHITE else Color.BLACK)

                boardView.pieceText.text = piece.char.toString()
                boardView.piece.isVisible = piece != Piece.Empty
            }
        }
    }

    private fun addPieceToTrade(boardSlotView: BoardSlotView) {
        val playerPiece = gamePlayer.getOwnPlayer().piece
        val line = boardSlotView.vector.y
        val column = boardSlotView.vector.x
        val boardPiece = gamePlayer.getGameBoard()[line][column]

        if (!tradePieces.contains(boardSlotView)) {
            if (tradePieces.size < 2 && playerPiece == boardPiece) {
                tradePieces.add(boardSlotView)
                boardSlotView.slot.setBackgroundResource(R.color.trade_board_background)
            } else if (tradePieces.size == 2 && playerPiece != boardPiece) {
                tradePieces.add(boardSlotView)
                boardSlotView.slot.setBackgroundResource(R.color.trade_board_background)
            }
        } else {
            tradePieces.remove(boardSlotView)
            boardSlotView.slot.setBackgroundResource(R.drawable.square_border)
        }
    }

    fun refreshBoard() {
        updatePieces()
        showPossibleMoves()
    }

    data class BoardSlotView(
        val slot: ViewGroup,
        val piece: View,
        val pieceText: TextView,
        val vector: Vector,
    )
}