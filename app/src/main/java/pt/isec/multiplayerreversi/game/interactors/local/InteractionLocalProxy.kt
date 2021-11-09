package pt.isec.multiplayerreversi.game.interactors.local

import pt.isec.multiplayerreversi.game.interactors.InteractionProxy
import pt.isec.multiplayerreversi.game.logic.Piece
import pt.isec.multiplayerreversi.game.logic.Player
import pt.isec.multiplayerreversi.game.logic.Vector

class InteractionLocalProxy : InteractionProxy {
    //TODO talvez dividir em 2 interfaces cenas de enviar jogadas e cenas de receber jogadas

    init {
        //TODO Make the stufss
    }

    private lateinit var possibleMovesCallback: (List<Vector>) -> Unit
    private lateinit var updateBoardCallback: (Array<Array<Piece>>) -> Unit
    private lateinit var changePlayerCallback: (Int) -> Unit

    private lateinit var players: ArrayList<Player>
    private lateinit var player: Player

    //TODO 20 detach from game all the callbacks

    override fun playAt(line: Int, column: Int) {
        //TODO 5 send through the socket
    }

    override fun getPlayers() = players

    override fun getOwnPlayer() = player

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