package pt.isec.multiplayerreversi.game.interactors.networking

import android.util.Log
import pt.isec.multiplayerreversi.App
import pt.isec.multiplayerreversi.App.Companion.OURTAG
import pt.isec.multiplayerreversi.App.Companion.LISTENING_PORT
import pt.isec.multiplayerreversi.game.logic.Game
import pt.isec.multiplayerreversi.game.logic.Piece
import pt.isec.multiplayerreversi.game.logic.Player
import java.io.Closeable
import java.lang.Exception
import java.net.ServerSocket
import java.net.SocketException
import kotlin.concurrent.thread

class ConnectionsWelcomer(
    val app: App,
    private val players: ArrayList<Player>,
) : Closeable {

    data class PlayerSetuper(val player: Player, val setuper: GamePlayerHostSide)

    private var receivingThread: Thread? = null
    private var started: Boolean = false
    private val setupers = HashMap<Int, PlayerSetuper>()

    private var serverSocket: ServerSocket? = null
    private var shouldClose = false
    private var playersAmount = players.size

    lateinit var playersChanged: (Int) -> Unit

    init {
        listenForConnections()
    }

    private fun listenForConnections() {
        if (receivingThread != null) return
        serverSocket = ServerSocket(LISTENING_PORT)
        receivingThread = thread {
            println("Listening for connections")
            while (!shouldClose) {
                try {
                    serverSocket?.let {
                        val socket = serverSocket!!.accept()
                        Log.i(OURTAG, "Recieved connection from ip: " + socket.inetAddress)
                        playersAmount++

                        thread {
                            try {
                                GamePlayerHostSide(app, socket, this) { playerId ->
                                    val player = players.find { it.playerId == playerId }
                                    if (player != null)
                                        Log.i(OURTAG, player.toString())
                                    else
                                        Log.i(OURTAG,
                                            "player that got ready with id $playerId is null for some reason that is unknown to the writer of this message")
                                }
                            } catch (e: Exception) {
                                Log.e(OURTAG, "Exception", e)
                            }
                        }

                        if (playersAmount >= Game.PLAYER_LIMIT) {
                            shouldClose = true
                            serverSocket!!.close()
                            serverSocket = null
                            receivingThread = null
                        }
                    }
                } catch (e: SocketException) {
                    shouldClose = true
                }
            }
        }
    }

    fun getPlayers() = players

    fun joinPlayer(newPlayer: Player, setuper: GamePlayerHostSide) {
        val freePiece: Piece? = Piece.getPieces().find { thisPiece ->
            val noPLayerHasPiece = players.find { p -> p.piece == thisPiece } == null
            noPLayerHasPiece
        }
        newPlayer.piece = freePiece
            ?: throw IllegalStateException("Player was joining and there were no more free pieces")

        setupers.forEach { it.value.setuper.arrivedPlayer(newPlayer) }

        players.add(newPlayer)
        setupers[newPlayer.playerId] = PlayerSetuper(newPlayer, setuper)

        playersChanged()
    }

    fun sendStart(game: Game) {
        setupers.forEach {
            it.value.setuper.game = game
            it.value.player.callbacks = it.value.setuper
            it.value.setuper.sendStart(game)
        }
        started = true
    }

    override fun close() {
        if (!started) setupers.forEach {
            it.value.setuper.sendExit()
            it.value.setuper.connectionsWelcomer = null
        }
        serverSocket?.close()
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
        playersAmount--
        playersChanged()
    }

    private fun playersChanged() {
        if (players.size <= 2) {
            shouldClose = false
            listenForConnections()
        }
        playersChanged(players.size)
    }
}