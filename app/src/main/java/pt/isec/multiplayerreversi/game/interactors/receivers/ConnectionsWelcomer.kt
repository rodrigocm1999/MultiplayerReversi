package pt.isec.multiplayerreversi.game.interactors.receivers

import java.net.ServerSocket

val port = 43578

class ConnectionsWelcomer  : Thread() {

    private val serverSocket = ServerSocket(port)

    override fun run() {
        super.run()

        val socket = serverSocket.accept()

    }
}