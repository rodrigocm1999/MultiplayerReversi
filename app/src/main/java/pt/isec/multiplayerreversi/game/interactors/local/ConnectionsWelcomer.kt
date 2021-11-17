package pt.isec.multiplayerreversi.game.interactors.local

import pt.isec.multiplayerreversi.game.interactors.remote.RemotePlayerProxy
import pt.isec.multiplayerreversi.game.logic.Piece
import pt.isec.multiplayerreversi.game.logic.Player
import pt.isec.multiplayerreversi.App.Companion.listeningPort
import java.io.Closeable
import java.net.ServerSocket

class ConnectionsWelcomer(
    private val players: ArrayList<Player>,
    private val callback: (RemotePlayerProxy) -> Unit,
) : Thread(), Closeable {

    private val serverSocket = ServerSocket(listeningPort)

    override fun run() {
        super.run()

        val socket = serverSocket.accept()

        val t = Thread {
            //TODO 3  passar informções acerca do jogo a cada remote player
            val p = RemotePlayerProxy(socket, this)
            callback(p)//Se calhar devolver a interactionproxy ou whatever
        }
        t.start()
    }

    fun getPlayers() = players

    fun joinPlayer(newPlayer: Player) {
        val otherPlayer = players[players.lastIndex]
        newPlayer.piece = Piece.getByOrdinal(otherPlayer.piece.ordinal + 1)
            ?: throw IllegalStateException("Player was joining and there were no more free pieces")
        players.add(newPlayer)
    }

    override fun close() {
        serverSocket.close()
    }
}