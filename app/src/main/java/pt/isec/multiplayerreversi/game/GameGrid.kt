package pt.isec.multiplayerreversi.game

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


class GameGrid(
    context: Context,
    private val gridLayout: GridLayout,
    screenSize: DisplayMetrics,
    layoutInflater: LayoutInflater,
    private val boardSideLength: Int,
    private val gamePlayer: GamePlayer,
) {

    private val grid: Array<Array<BoardSlotView>>
    private var possibleMoves: List<Vector>? = null

    private val darkPiece =
        AppCompatResources.getDrawable(context, R.drawable.piece_dark)
    private val lightPiece =
        AppCompatResources.getDrawable(context, R.drawable.piece_light)
    private val bluePiece =
        AppCompatResources.getDrawable(context, R.drawable.piece_blue)
    private val possiblePieceDark =
        AppCompatResources.getDrawable(context, R.drawable.piece_possible_black)
    private val possiblePieceLight =
        AppCompatResources.getDrawable(context, R.drawable.piece_possible_white)
    private val possiblePieceBlue =
        AppCompatResources.getDrawable(context, R.drawable.piece_possible_blue)

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

        //Threads are not faster in this case and layoutinflater should only be used by 1 thread
//        val syncList = ConcurrentLinkedQueue<BoardSlotView>()
//        val worker = Runnable {
//            val view = layoutInflater.inflate(R.layout.piece_layout, null) as ViewGroup
//            view.layoutParams = ViewGroup.LayoutParams(sideLength, sideLength)
//            val boardView = BoardSlotView(view, view[0], view[1] as TextView)
//            syncList.add(boardView)
//        }
//        val executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
//        for (i in 0 until boardSideLength * boardSideLength) executor.execute(worker)
//        executor.shutdown()
//        executor.awaitTermination(1, TimeUnit.DAYS)

        grid = Array(boardSideLength) { line ->
            Array(boardSideLength) { column ->
//                val boardView = syncList.remove()
//                val view = boardView.slot
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

        gamePlayer.updateBoardCallback = {
            updatePieces(it)
        }
        gamePlayer.possibleMovesCallback = {
            showPossibleMoves(it, gamePlayer.getOwnPlayer().piece)
        }
    }

    fun clearPossibleMoves(): List<Vector>? {
        possibleMoves?.forEach {
            val boardSlot = grid[it.y][it.x]
            boardSlot.piece.isVisible = false
        }
        return possibleMoves
    }

    fun showPossibleMoves(list: List<Vector>?, currentPiece: Piece) {
        val temp = when (currentPiece) {
            Piece.Dark -> possiblePieceDark
            Piece.Light -> possiblePieceLight
            Piece.Blue -> possiblePieceBlue
            else -> possiblePieceDark
        }
        if (list == null) return
        for (it in list) {
            val boardSlot = grid[it.y][it.x]
            boardSlot.piece.background = temp
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
                val boardView = grid[line][column]

                boardView.piece.background = when (piece) {
                    Piece.Dark -> darkPiece
                    Piece.Light -> lightPiece
                    Piece.Blue -> bluePiece
                    else -> darkPiece
                }
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

    data class BoardSlotView(val slot: ViewGroup, val piece: View, val pieceText: TextView)
}