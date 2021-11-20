package pt.isec.multiplayerreversi.game.interactors

import pt.isec.multiplayerreversi.game.logic.Game
import pt.isec.multiplayerreversi.game.logic.Vector

open class Local1V1Play(protected val game: Game) : AbstractCallBacks(), GamePlayer {

    override fun playAt(line: Int, column: Int) {
        game.playAt(getOwnPlayer(), line, column)
    }

    override fun playBomb(line: Int, column: Int) {
        game.playBombPiece(getOwnPlayer(), line, column)
    }

    override fun playTrade(tradePieces: ArrayList<Vector>) {
        game.playTrade(tradePieces)
    }

    override fun getPlayers() = game.getPlayers()
    override fun getOwnPlayer() = game.getCurrentPlayer()
    override fun getGameBoard() = game.getBoard()
    override fun getGameSideLength() = game.getSideLength()
}