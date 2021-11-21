package pt.isec.multiplayerreversi.game.interactors

import android.util.Log
import pt.isec.multiplayerreversi.App.Companion.OURTAG
import pt.isec.multiplayerreversi.App.Companion.listeningPort
import pt.isec.multiplayerreversi.game.logic.Piece
import pt.isec.multiplayerreversi.game.logic.Player
import java.io.Closeable
import java.net.ServerSocket
import java.net.SocketException
import kotlin.concurrent.thread

class ConnectionsWelcomer(
    private val players: ArrayList<Player>,
    private val callback: (RemotePlayerProxy) -> Unit,
) : Closeable {

    private val serverSocket = ServerSocket(listeningPort)
    private var shouldClose = false
    private var playersAmount = players.size

    init {
        thread {
            println("Listening for connections")
            while (!shouldClose) {
                try {
                    val socket = serverSocket.accept()
                    Log.i(OURTAG, "Recieved connection from ip: " + socket.inetAddress)
                    playersAmount++

                    thread {
                        val p = RemotePlayerProxy(socket, this)
                        callback(p)
                    }

                    if (playersAmount >= 3)
                        shouldClose = true

                } catch (e: SocketException) {
                    shouldClose = true
                }
            }
            Log.i(OURTAG,"ServerSocket Closed")
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