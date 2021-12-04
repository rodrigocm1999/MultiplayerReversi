package pt.isec.multiplayerreversi.game

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.get
import androidx.core.view.isVisible
import pt.isec.multiplayerreversi.R
import pt.isec.multiplayerreversi.game.interactors.GamePlayer
import pt.isec.multiplayerreversi.game.logic.Piece
import pt.isec.multiplayerreversi.game.logic.Vector
import kotlin.concurrent.thread
import kotlin.system.exitProcess


class GameGrid(
    private val context: Activity,
    private val gridLayout: GridLayout,
    screenSize: DisplayMetrics,
    layoutInflater: LayoutInflater,
    private val boardSideLength: Int,
    private val gamePlayer: GamePlayer,
) {

    private val grid: Array<Array<BoardSlotView>>

    var isUsingBombPiece = false
    var isUsingTrade = false

    private val tradePieces = ArrayList<Vector>(3)


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
                val boardView = BoardSlotView(view, view[0], view[1] as TextView)

                view.setOnClickListener {
                    when {
                        isUsingBombPiece -> gamePlayer.playBomb(line, column)
                        isUsingTrade -> {
                            addPieceToTrade(line, column)
                            if (tradePieces.size == 3) {
                                gamePlayer.playTrade(tradePieces)
                                tradePieces.clear()
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

    fun clearPossibleMoves(): List<Vector>? {
        gamePlayer.getPossibleMoves().forEach {
            val boardSlot = grid[it.y][it.x]
            boardSlot.piece.isVisible = false
        }
        return gamePlayer.getPossibleMoves()
    }

    fun showPossibleMoves() {
        val temp = gamePlayer.getOwnPlayer().piece.getPossibleDrawable(context)
        for (it in gamePlayer.getPossibleMoves()) {
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

    private fun addPieceToTrade(line: Int, column: Int) {
        val playerPiece = gamePlayer.getOwnPlayer().piece
        val boardPiece = gamePlayer.getGameBoard()[line][column]
        val v = Vector(column, line)

        if (!tradePieces.contains(v)) {
            if (tradePieces.size < 2 && playerPiece == boardPiece)
                tradePieces.add(v)
            else if (playerPiece != boardPiece)
                tradePieces.add(v)
        }
    }

    fun refreshBoard() {
        updatePieces()
        showPossibleMoves()
    }

    data class BoardSlotView(val slot: ViewGroup, val piece: View, val pieceText: TextView)
}