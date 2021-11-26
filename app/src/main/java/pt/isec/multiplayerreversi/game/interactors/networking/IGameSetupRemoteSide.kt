package pt.isec.multiplayerreversi.game.interactors.networking

import pt.isec.multiplayerreversi.game.interactors.GamePlayer
import pt.isec.multiplayerreversi.game.logic.Player

interface IGameSetupRemoteSide {
    val arrivedPlayerCallback: ((Player) -> Unit)
    val leftPlayerCallback: ((Player) -> Unit)
    val hostExitedCallback: (() -> Unit)
    val gameStartingCallback: ((GamePlayer) -> Unit)

    fun leave()
    fun getPlayers(): List<Player>
}
