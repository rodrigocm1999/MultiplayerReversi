package pt.isec.multiplayerreversi.game.interactors.local

import pt.isec.multiplayerreversi.game.logic.Player

class InteractionLocalRemoteGameProxy : AbstractLocalProxy() {

    private lateinit var players: ArrayList<Player>

    private lateinit var player: Player
    private val gameSideLength: Int = 0

    init {
        //TODO Make the stufss
    }

    override fun playAt(line: Int, column: Int) {
        //TODO 5 send through the socket
    }

    override fun playBomb(line: Int, column: Int) {
    }

    override fun getGameSideLength() = gameSideLength

    override fun getPlayers() = players

    override fun getOwnPlayer() = player

}