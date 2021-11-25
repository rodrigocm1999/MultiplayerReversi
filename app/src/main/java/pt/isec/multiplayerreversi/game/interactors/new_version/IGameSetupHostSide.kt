package pt.isec.multiplayerreversi.game.interactors.new_version

import pt.isec.multiplayerreversi.game.logic.Game
import pt.isec.multiplayerreversi.game.logic.Player

interface IGameSetupHostSide {
    val readyUpCallback: ((Int) -> Unit) // receives player Id

    fun arrivedNewPlayer(player: Player)
    fun sendExit()
    fun sendStart(game: Game)
}