package pt.isec.multiplayerreversi.game.interactors.local

import pt.isec.multiplayerreversi.game.interactors.InteractionProxy
import pt.isec.multiplayerreversi.game.logic.Game
import pt.isec.multiplayerreversi.game.logic.Piece
import pt.isec.multiplayerreversi.game.logic.Vector

open class Local1V1Interaction(protected val game: Game) : InteractionProxy {

    init {
        game.registerListener(Game.showMovesEvent) {
            possibleMovesCallback?.let { it1 -> it1(it.newValue as ArrayList<Vector>) }
        }
        game.registerListener(Game.updateBoardEvent) {
            updateBoardCallback?.let { it1 -> it1(it.newValue as Array<Array<Piece>>) }
        }
        game.registerListener(Game.updateCurrentPlayerEvent) {
            changePlayerCallback?.let { it1 -> it1(it.newValue as Int) }
        }
    }

    private var possibleMovesCallback: ((List<Vector>) -> Unit)? = null
    private var updateBoardCallback: ((Array<Array<Piece>>) -> Unit)? = null
    private var changePlayerCallback: ((Int) -> Unit)? = null

    //TODO 20 detach from game all the callbacks

    override fun playAt(line: Int, column: Int) {
        game.playAt(game.getCurrentPlayer(), line, column)
    }

    override fun playBomb(line: Int, column: Int) {
        game.playBombPiece(game.getCurrentPlayer(),line,column)
    }

    override fun getPlayers() = game.getPlayers()

    override fun getOwnPlayer() = game.getCurrentPlayer()

    override fun setPossibleMovesCallBack(consumer: (List<Vector>) -> Unit) {
        possibleMovesCallback = consumer
    }

    override fun setUpdateBoardEvent(consumer: (Array<Array<Piece>>) -> Unit) {
        updateBoardCallback = consumer
    }

    override fun setChangePlayerCallback(consumer: (Int) -> Unit) {
        changePlayerCallback = consumer
    }

}