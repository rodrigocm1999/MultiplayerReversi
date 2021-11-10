package pt.isec.multiplayerreversi.game.interactors

import pt.isec.multiplayerreversi.game.logic.GameEndStats
import pt.isec.multiplayerreversi.game.logic.Piece
import pt.isec.multiplayerreversi.game.logic.Player
import pt.isec.multiplayerreversi.game.logic.Vector
import java.io.Serializable

interface InteractionProxy : Serializable {

    fun playAt(line: Int, column: Int)
    fun playBomb(line: Int, column: Int)
    //fun playTrade(line: Int, column: Int,line: Int, column: Int,line: Int, column: Int)
    //TODO 1 adicionar as funções para as jogadas especiais


    fun getPlayers(): List<Player>
    fun getOwnPlayer(): Player
    fun getPlayerById(id: Int): Player?

    fun setPossibleMovesCallBack(consumer: (List<Vector>) -> Unit)
    fun setUpdateBoardEvent(consumer: (Array<Array<Piece>>) -> Unit)
    fun setChangePlayerCallback(consumer: (Int) -> Unit)
    fun setGameFinishedCallback(consumer: (GameEndStats) -> Unit)

    // TODO 2 usar estes callbacks para atualizar a interface e atualizar os botões
    //fun setPlayerUsedTradeCallback(consumer: (Int) -> Unit)
    //fun setPlayerUsedBombCallback(consumer: (Int) -> Unit)

    fun getGameSideLength(): Int
}