package pt.isec.multiplayerreversi.game.interactors.new_version

import android.util.Log
import pt.isec.multiplayerreversi.App.Companion.OURTAG
import pt.isec.multiplayerreversi.App.Companion.listeningPort
import pt.isec.multiplayerreversi.game.logic.Game
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

    private var started: Boolean = false
    private val setupers = HashMap<Int, PlayerSetuper>() //= Set<PlayerSetuper>(3)

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
                            Log.i(OURTAG, "Player got ready")
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

        setupers.forEach { it.value.setuper.arrivedPlayer(newPlayer) }

        players.add(newPlayer)
        setupers.put(newPlayer.playerId, PlayerSetuper(newPlayer, setuper))
    }

    fun sendStart(game: Game) {
        setupers.forEach {
            it.value.setuper.sendStart(game)
            it.value.player.callbacks = it.value.setuper.createGamePlayer(game)
        }
        started = true
    }

    override fun close() {
        if (!started) setupers.forEach {
            it.value.setuper.sendExit()
        }
        serverSocket.close()
    }

    fun playerLeft(player: Player) {
        for (i in 0 until players.size) {
            if (players[i].playerId == player.playerId) {
                players.removeAt(i)
                break
            }
        }
        setupers.remove(player.playerId)
        setupers.forEach {
            it.value.setuper.leftPayer(player.playerId)
        }
    }
}