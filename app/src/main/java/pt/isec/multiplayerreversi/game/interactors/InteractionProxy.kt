package pt.isec.multiplayerreversi.game.interactors

import pt.isec.multiplayerreversi.game.logic.Piece
import pt.isec.multiplayerreversi.game.logic.Player
import pt.isec.multiplayerreversi.game.logic.Vector
import java.io.Serializable

interface InteractionProxy : Serializable {

    fun playAt(line: Int, column: Int)

    //TODO 1 adicionar as funções para as jogadas especiais


    fun getPlayers() : List<Player>
    fun getOwnPlayer() : Player

    fun setPossibleMovesCallBack(consumer: (List<Vector>) -> Unit)
    fun setUpdateBoardEvent(consumer: (Array<Array<Piece>>) -> Unit)
    fun setChangePlayerCallback(consumer: (Int) -> Unit)

    // TODO 2 usar estes callbacks para atualizar a interface e atualizar os botões
    //fun setPlayerUsedBombCallback(consumer: (Int) -> Unit)
    //fun setPlayerUsedTradeCallback(consumer: (Int) -> Unit)
}