package pt.isec.multiplayerreversi.game.interactors

import pt.isec.multiplayerreversi.game.logic.Game
import pt.isec.multiplayerreversi.game.logic.Player

class LocalOnline(game: Game, private val player: Player) : Local1V1Play(game) {


    override fun isOnline() = true
    override fun getOwnPlayer() = player
}