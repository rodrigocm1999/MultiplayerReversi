package pt.isec.multiplayerreversi.game.interactors.setup

import pt.isec.multiplayerreversi.game.interactors.GamePlayer
import pt.isec.multiplayerreversi.game.logic.Player

interface IGameSetupRemoteSide {
    val arrivedNewPlayerCallback: ((Player) -> Unit)
    val hostExitedCallback: (() -> Unit)
    val gameStartingCallback: ((GamePlayer) -> Unit)

    fun ready()
    fun getPlayers(): List<Player>
}

interface IGameSetupHostSide {
    val readyUpCallback: ((Int) -> Unit) // receives player Id

    fun arrivedNewPlayer(player: Player)
    fun sendExit()
    fun sendStart()
}