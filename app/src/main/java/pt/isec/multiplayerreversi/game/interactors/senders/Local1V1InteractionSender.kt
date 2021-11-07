package pt.isec.multiplayerreversi.game.interactors.senders

import pt.isec.multiplayerreversi.game.logic.Game
import pt.isec.multiplayerreversi.game.logic.Piece
import pt.isec.multiplayerreversi.game.logic.Vector

class Local1V1InteractionSender(private val game: Game) : InteractionSenderProxy {

    init {
        game.registerListener(Game.showMovesEvent) { possibleMovesCallback(it.newValue as ArrayList<Vector>) }
        game.registerListener(Game.updateBoardEvent) { updateBoardCallback(it.newValue as Array<Array<Piece>>) }
        game.registerListener(Game.updateCurrentPlayerEvent) { changePlayerCallback(it.newValue as Int) }
    }

    private lateinit var possibleMovesCallback: (List<Vector>) -> Unit
    private lateinit var updateBoardCallback: (Array<Array<Piece>>) -> Unit
    private lateinit var changePlayerCallback: (Int) -> Unit

    //TODO 20 detach from game all the callbacks

    override fun playAt(line: Int, column: Int) {
        game.playAt(game.getCurrentPlayer(), line, column)
    }

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