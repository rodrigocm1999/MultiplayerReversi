package pt.isec.multiplayerreversi.game.interactors

import pt.isec.multiplayerreversi.game.logic.Game
import pt.isec.multiplayerreversi.game.logic.GameEndStats
import pt.isec.multiplayerreversi.game.logic.Piece
import pt.isec.multiplayerreversi.game.logic.Vector

open class LocalPlayer(protected val game: Game) : GamePlayer {

    protected var alreadyReady = false

    override fun playAt(line: Int, column: Int) {
        game.playAt(getOwnPlayer(), line, column)
    }

    override fun playBomb(line: Int, column: Int) {
        game.playBombPiece(getOwnPlayer(), line, column)
    }

    override fun playTrade(tradePieces: ArrayList<Vector>) {
        game.playTrade(getOwnPlayer(), tradePieces)
    }

    override fun ready() {
        if (alreadyReady) return
        alreadyReady = true
        game.players.forEach { game.playerReady(it) }
    }

    override fun leaveGame() {} // No need to do anything
    override fun passPlayer() {
        game.passPlayer(getOwnPlayer())
    }

    override fun isOnline() = false
    override fun getPlayers() = game.players
    override fun getCurrentPlayer() = game.currentPlayer
    override fun getOwnPlayer() = game.currentPlayer
    override fun getGameData() = game.gameData
    override fun getGameBoard() = game.board
    override fun getGameSideLength() = game.sideLength
    override fun getPossibleMoves() =
        if (game.gameSettings.showPossibleMoves) game.currentPlayerPossibleMoves else ArrayList()

    override var possibleMovesCallback: ((List<Vector>) -> Unit)? = null
    override var updateBoardCallback: ((Array<Array<Piece>>) -> Unit)? = null
    override var changePlayerCallback: ((Int) -> Unit)? = null
    override var gameFinishedCallback: ((GameEndStats) -> Unit)? = null
    override var playerUsedBombCallback: ((Int) -> Unit)? = null
    override var playerUsedTradeCallback: ((Int) -> Unit)? = null
    override var gameTerminatedCallback: (() -> Unit)? = null
}