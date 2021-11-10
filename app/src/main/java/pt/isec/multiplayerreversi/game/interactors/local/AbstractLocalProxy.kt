package pt.isec.multiplayerreversi.game.interactors.local

import pt.isec.multiplayerreversi.game.interactors.InteractionProxy
import pt.isec.multiplayerreversi.game.logic.Piece
import pt.isec.multiplayerreversi.game.logic.Vector


abstract class AbstractLocalProxy : InteractionProxy {

    protected var possibleMovesCallback: ((List<Vector>) -> Unit)? = null
    protected var updateBoardCallback: ((Array<Array<Piece>>) -> Unit)? = null
    protected var changePlayerCallback____: ((Int) -> Unit)? = null

    //TODO 20 detach from game all the callbacks

    override fun getPlayerById(id: Int) = getPlayers().find { p -> p.getPlayerId() == id }

    override fun setPossibleMovesCallBack(consumer: (List<Vector>) -> Unit) {
        possibleMovesCallback = consumer
    }

    override fun setUpdateBoardEvent(consumer: (Array<Array<Piece>>) -> Unit) {
        updateBoardCallback = consumer
    }

    override fun setChangePlayerCallback(consumer: (Int) -> Unit) {
        changePlayerCallback____ = consumer
    }

}
