package pt.isec.multiplayerreversi.game.interactors.local

import pt.isec.multiplayerreversi.game.interactors.InteractionProxy
import pt.isec.multiplayerreversi.listeningPort
import java.net.ServerSocket

class ConnectionsWelcomer(private val callback: (InteractionProxy) -> Unit) : Thread() {

    private val serverSocket = ServerSocket(listeningPort)

    override fun run() {
        super.run()

        val socket = serverSocket.accept()

        val t = Thread {
            //TODO 3  passar informções acerca do jogo a cada remote player
            val p = InteractionLocalProxy()
            callback(p)//Se calhar devolver a interactionproxy ou whatever
        }
        t.start()
    }
}