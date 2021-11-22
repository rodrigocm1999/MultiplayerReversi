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
}

interface GameDataGetter {
    fun getPlayers(): List<Player>
    fun getOwnPlayer(): Player
    fun getGameBoard(): Array<Array<Piece>>

    fun getPlayerById(id: Int): Player? = getPlayers().find { p -> p.playerId == id }
    fun getGameSideLength(): Int = getGameBoard().size
}

interface GameCallbacks {
    var possibleMovesCallback: ((List<Vector>) -> Unit)?
    var updateBoardCallback: ((Array<Array<Piece>>) -> Unit)?
    var changePlayerCallback: ((Int) -> Unit)?
    var gameFinishedCallback: ((GameEndStats) -> Unit)?
    var playerUsedBombCallback: ((Int) -> Unit)?
    var playerUsedTradeCallback: ((Int) -> Unit)?
}