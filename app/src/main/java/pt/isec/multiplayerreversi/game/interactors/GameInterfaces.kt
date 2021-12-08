package pt.isec.multiplayerreversi.game.interactors

import pt.isec.multiplayerreversi.game.logic.*
import pt.isec.multiplayerreversi.game.logic.Vector
import java.util.*

interface GamePlayer : GameDataGetter, GameCallbacks {
    fun playAt(line: Int, column: Int)
    fun playBomb(line: Int, column: Int)
    fun playTrade(tradePieces: ArrayList<Vector>)

    fun ready()
    fun leaveGame()
    fun passPlayer()
}

interface GameDataGetter {
    fun isOnline(): Boolean
    fun getPlayers(): List<Player>
    fun getCurrentPlayer(): Player
    fun getOwnPlayer(): Player
    fun getGameData(): GameData
    fun getGameBoard(): Array<Array<Piece>>

    fun getPossibleMoves(): List<Vector>
    fun getPlayerById(id: Int): Player? = getPlayers().find { p -> p.playerId == id }
    fun getGameSideLength(): Int = getGameBoard().size

    fun playerHasAnyMoves(): Boolean {
        return getPossibleMoves().isNotEmpty() || getOwnPlayer().canUseTrade() ||
                getOwnPlayer().canUseBomb() || playerHasPiecesOnBoard(getOwnPlayer().piece)
    }

    fun playerHasPiecesOnBoard(piece: Piece): Boolean {
        val board = getGameBoard()
        val sideLength = getGameSideLength()
        for (line in 0 until sideLength)
            for (column in 0 until sideLength)
                if (board[line][column] == piece)
                    return true
        return false
    }
}

interface GameCallbacks {
    var possibleMovesCallback: ((List<Vector>) -> Unit)?
    var updateBoardCallback: ((Array<Array<Piece>>) -> Unit)?
    var changePlayerCallback: ((Int) -> Unit)?
    var gameFinishedCallback: ((GameEndStats) -> Unit)?
    var playerUsedBombCallback: ((Int) -> Unit)?
    var playerUsedTradeCallback: ((Int) -> Unit)?
    var gameTerminatedCallback: (() -> Unit)?
}