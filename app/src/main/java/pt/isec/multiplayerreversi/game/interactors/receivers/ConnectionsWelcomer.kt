package pt.isec.multiplayerreversi.game.interactors.receivers

import pt.isec.multiplayerreversi.listeningPort
import java.net.ServerSocket
import java.net.Socket

class ConnectionsWelcomer(private val callback: (Socket) -> Unit) : Thread() {

    private val serverSocket = ServerSocket(listeningPort)

    override fun run() {
        super.run()

        val socket = serverSocket.accept()

        val t = Thread {
            //TODO 3  passar informções acerca do jogo a cada remote player
            callback(socket)//Se calhar devolver a interactionproxy ou whatever
        }
        t.start()
    }
}