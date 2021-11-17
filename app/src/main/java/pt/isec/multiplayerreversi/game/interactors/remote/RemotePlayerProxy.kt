package pt.isec.multiplayerreversi.game.interactors.remote

import pt.isec.multiplayerreversi.AbstractNetworkingProxy
import pt.isec.multiplayerreversi.game.interactors.local.ConnectionsWelcomer
import pt.isec.multiplayerreversi.game.logic.Piece
import pt.isec.multiplayerreversi.game.logic.Player
import pt.isec.multiplayerreversi.game.logic.Vector
import java.io.Closeable
import java.net.Socket
import java.util.ArrayList


open class RemotePlayerProxy(socket: Socket, connectionsWelcomer: ConnectionsWelcomer) :
    AbstractNetworkingProxy(socket), Closeable {

    //Sequence:
    //Host sends current players
    //Remote send profile
    //Host send new player Ids
    //Remote .......
    //TODO host sends update to other players that new player has entered

    init {
        sendPlayers(connectionsWelcomer.getPlayers())

        jsonReader.beginObject()
        val newPlayer = Player()
        readPlayer(newPlayer)
        //Player object gets its fields filled up
        connectionsWelcomer.joinPlayer(newPlayer)
        jsonReader.endObject()

        sendPlayerIds(newPlayer.playerId, newPlayer.piece)

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