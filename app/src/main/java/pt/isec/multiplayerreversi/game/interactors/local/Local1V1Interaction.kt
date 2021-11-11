package pt.isec.multiplayerreversi.game.interactors.local

import pt.isec.multiplayerreversi.game.logic.Game
import pt.isec.multiplayerreversi.game.logic.GameEndStats
import pt.isec.multiplayerreversi.game.logic.Piece
import pt.isec.multiplayerreversi.game.logic.Vector

open class Local1V1Interaction(protected val game: Game) : AbstractCallbacksProxy() {

    init {
        game.registerListener(Game.showMovesEvent) {
            _possibleMovesCallback?.let { it1 -> it1(it.newValue as ArrayList<Vector>) }
        }
        game.registerListener(Game.updateBoardEvent) {
            _updateBoardCallback?.let { it1 -> it1(it.newValue as Array<Array<Piece>>) }
        }
        game.registerListener(Game.updateCurrentPlayerEvent) {
            _changePlayerCallback?.let { it1 -> it1(it.newValue as Int) }
        }
        game.registerListener(Game.gameFinishedEvent) {
            _gameFinishedCallback?.let { it1 -> it1(it.newValue as GameEndStats) }
        }
    }

    //TODO 20 detach from game all the callbacks

    override fun playAt(line: Int, column: Int) {
        game.playAt(getOwnPlayer(), line, column)
    }

    override fun playBomb(line: Int, column: Int) {
        game.playBombPiece(getOwnPlayer(), line, column)
    }

    override fun playTrade(tradePieces: java.util.ArrayList<Vector>) {
        game.playTrade(tradePieces)
    }

    override fun getPlayers() = game.getPlayers()

    override fun getOwnPlayer() = game.getCurrentPlayer()
    override fun getGameBoard()= game.getBoard()

    override fun getGameSideLength() = game.getSideLength()
}