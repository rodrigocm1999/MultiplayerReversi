package pt.isec.multiplayerreversi.game.interactors.networking

import pt.isec.multiplayerreversi.game.logic.Game
import pt.isec.multiplayerreversi.game.logic.Player
import java.io.Closeable

interface IGameSetupHostSide : Closeable {
    val readyUpCallback: ((Int) -> Unit) // receives player Id

    fun arrivedPlayer(player: Player)
    fun leftPayer(playerId: Int)
    fun sendExit()
    fun sendStart(game: Game)
}