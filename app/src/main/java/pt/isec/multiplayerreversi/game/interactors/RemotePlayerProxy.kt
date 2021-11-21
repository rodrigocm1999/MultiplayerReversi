package pt.isec.multiplayerreversi.game.interactors

import pt.isec.multiplayerreversi.game.logic.Piece
import pt.isec.multiplayerreversi.game.logic.Player
import pt.isec.multiplayerreversi.game.logic.Vector
import java.io.Closeable
import java.net.Socket
import java.util.*


class RemotePlayerProxy(socket: Socket, connectionsWelcomer: ConnectionsWelcomer) :
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
        _player = Player()
        readPlayer(_player)
        println(_player)
        //Player object gets its fields filled up
        _player.callbacks = this
        connectionsWelcomer.joinPlayer(_player)
        endRead()

        beginSend()
        writePlayerIds(_player.playerId, _player.piece)
        endSend()

        println(_player)

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