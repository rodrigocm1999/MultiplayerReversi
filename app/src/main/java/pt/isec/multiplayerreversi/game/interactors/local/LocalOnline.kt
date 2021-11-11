package pt.isec.multiplayerreversi.game.interactors.local

import pt.isec.multiplayerreversi.game.logic.Game
import pt.isec.multiplayerreversi.game.logic.Player

class LocalOnline(game: Game, private val player: Player) : Local1V1Interaction(game) {

    override fun getOwnPlayer() = player
}