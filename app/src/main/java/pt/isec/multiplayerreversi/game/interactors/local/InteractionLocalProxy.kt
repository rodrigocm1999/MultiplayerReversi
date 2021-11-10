package pt.isec.multiplayerreversi.game.interactors.local

import pt.isec.multiplayerreversi.game.logic.Player

class InteractionLocalProxy : AbstractLocalProxy() {
    //TODO talvez dividir em 2 interfaces cenas de enviar jogadas e cenas de receber jogadas

    init {
        //TODO Make the stufss
    }

    private lateinit var players: ArrayList<Player>
    private lateinit var player: Player

    //TODO 20 detach from game all the callbacks

    override fun playAt(line: Int, column: Int) {
        //TODO 5 send through the socket
    }

    override fun playBomb(line: Int, column: Int) {
        TODO("Not yet implemented")
    }

    override fun getPlayers() = players

    override fun getOwnPlayer() = player

}