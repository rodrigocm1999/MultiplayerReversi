package pt.isec.multiplayerreversi.game.interactors.local

import pt.isec.multiplayerreversi.game.interactors.InteractionProxy
import pt.isec.multiplayerreversi.game.interactors.remote.InteractionRemotePlayerProxy
import pt.isec.multiplayerreversi.listeningPort
import java.io.Closeable
import java.net.ServerSocket

class ConnectionsWelcomer(private val callback: (InteractionRemotePlayerProxy) -> Unit) : Thread(), Closeable {

    private val serverSocket = ServerSocket(listeningPort)

    override fun run() {
        super.run()

        val socket = serverSocket.accept()

        val t = Thread {
            //TODO 3  passar informções acerca do jogo a cada remote player
            val p = InteractionRemotePlayerProxy(socket)
            callback(p)//Se calhar devolver a interactionproxy ou whatever
        }
        t.start()
    }

    override fun close() {
        serverSocket.close()
    }
}