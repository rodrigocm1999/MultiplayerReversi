package pt.isec.multiplayerreversi.game.interactors

import pt.isec.multiplayerreversi.game.logic.Game
import pt.isec.multiplayerreversi.game.logic.Player

class LocalOnline(game: Game, private val player: Player) : LocalPlayer(game) {
    override fun isOnline() = true
    override fun getOwnPlayer() = player

    override fun ready() {
        if (alreadyReady) return
        alreadyReady = true
        game.playerReady(getOwnPlayer())
    }

    override fun leaveGame() {
        game.playerLeaving(getOwnPlayer())
    }
}