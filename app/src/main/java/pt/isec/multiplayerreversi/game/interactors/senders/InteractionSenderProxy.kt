package pt.isec.multiplayerreversi.game.interactors.senders

import pt.isec.multiplayerreversi.game.logic.Piece
import pt.isec.multiplayerreversi.game.logic.Vector
import java.io.Serializable

interface InteractionSenderProxy : Serializable {

    fun playAt(line: Int, column: Int)

    fun setPossibleMovesCallBack(consumer: (List<Vector>) -> Unit)
    fun setUpdateBoardEvent(consumer: (Array<Array<Piece>>) -> Unit)
    fun setChangePlayerCallback(consumer: (Int) -> Unit)
}