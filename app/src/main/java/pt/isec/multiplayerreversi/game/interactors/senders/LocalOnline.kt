package pt.isec.multiplayerreversi.game.interactors.senders

import pt.isec.multiplayerreversi.game.logic.Game
import pt.isec.multiplayerreversi.game.logic.Player

class LocalOnline(game: Game, private val player: Player) : Local1V1Interaction(game) {

    override fun playAt(line: Int, column: Int) {
        game.playAt(player,line, column)
    }

    override fun getOwnPlayer() = player
}