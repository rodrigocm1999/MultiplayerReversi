package pt.isec.multiplayerreversi.game.interactors.new_version

import pt.isec.multiplayerreversi.game.interactors.GamePlayer
import pt.isec.multiplayerreversi.game.logic.Player

interface IGameSetupRemoteSide {
    val arrivedPlayerCallback: ((Player) -> Unit)
    val leftPlayerCallback: ((Player) -> Unit)
    val hostExitedCallback: (() -> Unit)
    val gameStartingCallback: ((GamePlayer) -> Unit)

    fun ready()
    fun getPlayers(): List<Player>
}
