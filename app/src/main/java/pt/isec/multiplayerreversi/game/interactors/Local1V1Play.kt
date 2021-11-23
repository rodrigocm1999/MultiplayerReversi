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

    override fun getPlayers() = game.players
    override fun getOwnPlayer() = game.currentPlayer
    override fun getGameBoard() = game.board
    override fun getGameSideLength() = game.sideLength
}