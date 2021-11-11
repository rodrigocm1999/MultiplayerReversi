package pt.isec.multiplayerreversi.game.interactors.local

import android.util.Log
import pt.isec.multiplayerreversi.AbstractNetworkingProxy
import pt.isec.multiplayerreversi.OURTAG
import pt.isec.multiplayerreversi.game.logic.Player
import pt.isec.multiplayerreversi.game.logic.Profile
import java.io.Closeable
import java.net.Socket
import java.net.SocketException

class LocalRemoteGameProxy(socket: Socket, profile: Profile) :
    AbstractNetworkingProxy(socket), Closeable {

    init {
        //TODO 10 do the initial handshake, receive profile
        _player = Player(profile)

        try {
            jsonReader.beginObject()
            _players = readPlayers()
            println(_players)
            jsonReader.endObject()

            sendProfile(profile)

            //Player object gets its fields filled up
            jsonReader.beginObject()
            readPlayerIds(_player)
            jsonReader.endObject()


            println(profile)

        } catch (e: SocketException) {
            Log.i(OURTAG, "Socket was close while creating LocalRemoteGameProxy")
        }
    }

    override fun playAt(line: Int, column: Int) {
        TODO("Not yet implemented")
    }

    override fun playBomb(line: Int, column: Int) {
        TODO("Not yet implemented")
    }


}