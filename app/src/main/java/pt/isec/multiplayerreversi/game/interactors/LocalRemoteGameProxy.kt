package pt.isec.multiplayerreversi.game.interactors

import android.util.Log
import pt.isec.multiplayerreversi.App.Companion.OURTAG
import pt.isec.multiplayerreversi.game.logic.Piece
import pt.isec.multiplayerreversi.game.logic.Player
import pt.isec.multiplayerreversi.game.logic.Profile
import pt.isec.multiplayerreversi.game.logic.Vector
import java.io.Closeable
import java.net.Socket
import java.net.SocketException
import java.util.*

class LocalRemoteGameProxy(socket: Socket, profile: Profile) :
    AbstractNetworkingProxy(socket), Closeable {

    private lateinit var board: Array<Array<Piece>>

    init {
        //TODO 10 do the initial handshake, receive profile
        _player = Player(profile)
        _players.add(_player)

        try {
            beginRead()
            _players.addAll(readPlayers())
            println(_players)
            endRead()

            beginSend()
            writeProfile(profile)
            endSend()

            //Player object gets its fields filled up
            beginRead()
            readPlayerIds(_player)
            endRead()


            println(profile)

        } catch (e: SocketException) {
            Log.i(OURTAG, "Socket was close while creating LocalRemoteGameProxy")
            throw e
        }
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

    override fun getGameBoard() = board


}