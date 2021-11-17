package pt.isec.multiplayerreversi.game.interactors.local

import android.util.Log
import pt.isec.multiplayerreversi.App.Companion.OURTAG
import pt.isec.multiplayerreversi.App.Companion.listeningPort
import pt.isec.multiplayerreversi.game.interactors.remote.RemotePlayerProxy
import pt.isec.multiplayerreversi.game.logic.Piece
import pt.isec.multiplayerreversi.game.logic.Player
import java.io.Closeable
import java.net.ServerSocket
import kotlin.concurrent.thread

class ConnectionsWelcomer(
    private val players: ArrayList<Player>,
    private val callback: (RemotePlayerProxy) -> Unit,
) : Thread(), Closeable {

    private val serverSocket = ServerSocket(listeningPort)
    private val shouldClose = false

    override fun run() {
        super.run()
        println("Listening for connections")
        while (!shouldClose) {
            val socket = serverSocket.accept()
            Log.i(OURTAG, "Recieved connection from ip: " + socket.inetAddress)

            thread {
                val p = RemotePlayerProxy(socket, this)
                callback(p)
            }
        }
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