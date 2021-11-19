package pt.isec.multiplayerreversi.game.interactors

import pt.isec.multiplayerreversi.game.logic.GameEndStats
import pt.isec.multiplayerreversi.game.logic.Piece
import pt.isec.multiplayerreversi.game.logic.Vector

abstract class CallBacksProxy : GamePlayer {

    override var possibleMovesCallback: ((List<Vector>) -> Unit)? = null
    override var updateBoardCallback: ((Array<Array<Piece>>) -> Unit)? = null
    override var changePlayerCallback: ((Int) -> Unit)? = null
    override var gameFinishedCallback: ((GameEndStats) -> Unit)? = null

//    protected var _possibleMovesCallback: ((List<Vector>) -> Unit)? = null
//    protected var _updateBoardCallback: ((Array<Array<Piece>>) -> Unit)? = null
//    protected var _changePlayerCallback: ((Int) -> Unit)? = null
//    protected var _gameFinishedCallback: ((GameEndStats) -> Unit)? = null
    // precisa do _ porque dá erro se não tiver, supostmante diz clashing declaration mas não existe isto em mais nenhum sitio do projeto
    // aconteçe por causa dos getters e setters do kotlin

//
//    final override fun setPossibleMovesCallback(consumer: (List<Vector>) -> Unit) {
//        _possibleMovesCallback = consumer
//    }
//
//    final override fun setBoardUpdatedCallback(consumer: (Array<Array<Piece>>) -> Unit) {
//        _updateBoardCallback = consumer
//    }
//
//    final override fun setChangePlayerCallback(consumer: (Int) -> Unit) {
//        _changePlayerCallback = consumer
//    }
//
//    final override fun setGameFinishedCallback(consumer: (GameEndStats) -> Unit) {
//        _gameFinishedCallback = consumer
//    }


}
