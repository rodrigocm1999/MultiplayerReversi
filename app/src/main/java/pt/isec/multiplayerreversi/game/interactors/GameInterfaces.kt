package pt.isec.multiplayerreversi.game.interactors

import pt.isec.multiplayerreversi.game.logic.GameEndStats
import pt.isec.multiplayerreversi.game.logic.Piece
import pt.isec.multiplayerreversi.game.logic.Player
import pt.isec.multiplayerreversi.game.logic.Vector
import java.util.*

interface GamePlayer : GameDataGetter, GameCallbacks {
    fun playAt(line: Int, column: Int)
    fun playBomb(line: Int, column: Int)
    fun playTrade(tradePieces: ArrayList<Vector>)

    fun ready()
    fun detach()
    fun passPlayer()
}

interface GameDataGetter {
    fun isOnline(): Boolean

    fun getPlayers(): List<Player>
    fun getCurrentPlayer(): Player
    fun getOwnPlayer(): Player
    fun getGameBoard(): Array<Array<Piece>>

    fun getPossibleMoves(): List<Vector>
    fun getPlayerById(id: Int): Player? = getPlayers().find { p -> p.playerId == id }
    fun getGameSideLength(): Int = getGameBoard().size
    fun playerHasNoMoves(): Boolean = getPossibleMoves().isEmpty()
}

interface GameCallbacks {
    var possibleMovesCallback: ((List<Vector>) -> Unit)?
    var updateBoardCallback: ((Array<Array<Piece>>) -> Unit)?
    var changePlayerCallback: ((Int) -> Unit)?
    var gameFinishedCallback: ((GameEndStats) -> Unit)?
    var playerUsedBombCallback: ((Int) -> Unit)?
    var playerUsedTradeCallback: ((Int) -> Unit)?
}