package pt.isec.multiplayerreversi.game.interactors.socket_related

import android.util.Log
import pt.isec.multiplayerreversi.App.Companion.OURTAG
import pt.isec.multiplayerreversi.App.Companion.listeningPort
import pt.isec.multiplayerreversi.game.interactors.new_version.GameSetupHostSide
import pt.isec.multiplayerreversi.game.interactors.setup.IGameSetupHostSide
import pt.isec.multiplayerreversi.game.logic.Piece
import pt.isec.multiplayerreversi.game.logic.Player
import java.io.Closeable
import java.net.ServerSocket
import java.net.SocketException
import kotlin.concurrent.thread

class ConnectionsWelcomer(
    private val players: ArrayList<Player>,
    private val callback: (GameSetupHostSide) -> Unit,
) : Closeable {

    data class PlayerSetuper(val player: Player, val setuper: IGameSetupHostSide)

    private val setupers = ArrayList<PlayerSetuper>(3)

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
                        val p = GameSetupHostSide(socket, this) { playerId ->
                            Log.i(OURTAG, "Player got ready ya know")
                            val player = players.find { it.playerId == playerId }
                            if (player != null)
                                Log.i(OURTAG, player.toString())
                            else
                                Log.i(OURTAG,
                                    "player that got ready with id $playerId is null for some reason that is unknow to the writer of this message")

                        }
                        callback(p)
                    }

                    if (playersAmount >= 3)
                        shouldClose = true

                } catch (e: SocketException) {
                    shouldClose = true
                }
            }
            Log.i(OURTAG, "ServerSocket Closed")
        }
    }

    fun getPlayers() = players

    fun joinPlayer(newPlayer: Player, setuper: IGameSetupHostSide) {
        val otherPlayer = players[players.lastIndex]
        newPlayer.piece = Piece.getByOrdinal(otherPlayer.piece.ordinal + 1)
            ?: throw IllegalStateException("Player was joining and there were no more free pieces")

        setupers.forEach {
            it.setuper.arrivedNewPlayer(newPlayer)
        }

        players.add(newPlayer)
        setupers.add(PlayerSetuper(newPlayer, setuper))
    }

    override fun close() {
        setupers.forEach {
            it.setuper.sendExit()
        }
        serverSocket.close()
    }
}