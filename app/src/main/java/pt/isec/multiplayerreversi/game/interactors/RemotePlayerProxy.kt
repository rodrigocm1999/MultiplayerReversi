package pt.isec.multiplayerreversi.game.interactors

import pt.isec.multiplayerreversi.game.logic.Piece
import pt.isec.multiplayerreversi.game.logic.Player
import pt.isec.multiplayerreversi.game.logic.Vector
import java.io.Closeable
import java.net.Socket
import java.util.*


open class RemotePlayerProxy(socket: Socket, connectionsWelcomer: ConnectionsWelcomer) :
    AbstractNetworkingProxy(socket), Closeable {

    //Sequence:
    //Host sends current players
    //Remote send profile
    //Host send new player Ids
    //Remote .......
    //TODO host sends update to other players that new player has entered

    init {
        beginSend()
        writePlayers(connectionsWelcomer.getPlayers())
        endSend()

        beginRead()
        val newPlayer = Player()
        readPlayer(newPlayer)
        println(newPlayer)
        //Player object gets its fields filled up
        connectionsWelcomer.joinPlayer(newPlayer)
        endRead()

        beginSend()
        writePlayerIds(newPlayer.playerId, newPlayer.piece)
        endSend()

        println(newPlayer)

        //TODO 20 update other clients with the new remote on their list
    }

    override fun playAt(line: Int, column: Int) {
        TODO("Not yet implemented")
    }

    override fun playBomb(line: Int, column: Int) {
        TODO("Not yet implemented")
    }

    override fun playTrade(tradePieces: ArrayList<Vector>) {
        TODO("Not yet implemented")
    }

    override fun getGameBoard(): Array<Array<Piece>> {
        TODO("Not yet implemented")
    }

}