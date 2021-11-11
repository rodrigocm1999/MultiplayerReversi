package pt.isec.multiplayerreversi.game.interactors.local

import pt.isec.multiplayerreversi.game.interactors.InteractionProxy
import pt.isec.multiplayerreversi.game.logic.GameEndStats
import pt.isec.multiplayerreversi.game.logic.Piece
import pt.isec.multiplayerreversi.game.logic.Vector

abstract class AbstractCallbacksProxy : InteractionProxy {

    protected var _possibleMovesCallback: ((List<Vector>) -> Unit)? = null
    protected var _updateBoardCallback: ((Array<Array<Piece>>) -> Unit)? = null
    protected var _changePlayerCallback: ((Int) -> Unit)? = null
    protected var _gameFinishedCallback: ((GameEndStats) -> Unit)? = null
    // precisa do _ porque dá erro se não tiver, supostmante diz clashing declaration mas não existe isto em mais nenhum sitio do projeto

    //TODO 20 detach from game all the callbacks

    override fun getPlayerById(id: Int) = getPlayers().find { p -> p.getPlayerId() == id }

    override fun setPossibleMovesCallBack(consumer: (List<Vector>) -> Unit) {
        _possibleMovesCallback = consumer
    }

    override fun setUpdateBoardEvent(consumer: (Array<Array<Piece>>) -> Unit) {
        _updateBoardCallback = consumer
    }

    override fun setChangePlayerCallback(consumer: (Int) -> Unit) {
        _changePlayerCallback = consumer
    }

    override fun setGameFinishedCallback(consumer: (GameEndStats) -> Unit) {
        _gameFinishedCallback = consumer
    }

}
