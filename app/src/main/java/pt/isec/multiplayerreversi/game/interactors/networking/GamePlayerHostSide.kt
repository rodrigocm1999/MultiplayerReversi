package pt.isec.multiplayerreversi.game.interactors.networking

import android.util.Log
import pt.isec.multiplayerreversi.App.Companion.OURTAG
import pt.isec.multiplayerreversi.game.interactors.GameCallbacks
import pt.isec.multiplayerreversi.game.interactors.GamePlayer
import pt.isec.multiplayerreversi.game.interactors.JsonTypes
import pt.isec.multiplayerreversi.game.logic.*
import java.lang.Exception
import java.net.Socket

class GamePlayerHostSide(
    socket: Socket,
    connectionsWelcomer: ConnectionsWelcomer,
    override val readyUpCallback: (Int) -> Unit,
) : AbstractNetworkingProxy(socket), GamePlayer, IGameSetupHostSide {


    private var ownPlayer: Player
    lateinit var game: Game

    private var alreadyReceivedReady = false

    init {
        beginSend()
        writePlayers(connectionsWelcomer.getPlayers())
        endSend()

        beginRead()
        ownPlayer = Player()
        readPlayer(ownPlayer)
        connectionsWelcomer.joinPlayer(ownPlayer, this)
        endRead()

        beginSend()
        writePlayerIds(ownPlayer.playerId, ownPlayer.piece)
        endSend()

        setReceiving("GameSetupHostSide receive") {
            try {
                while (!shouldExit) {
                    val type = beginReadAndGetType()
                    var readSomething = false
                    when (type) {
                        JsonTypes.Setup.LEFT_PLAYER -> {
                            val pId = jsonReader.nextInt()
                            readSomething = true
                            connectionsWelcomer.playerLeft(ownPlayer)
                        }
                        JsonTypes.InGame.NORMAL_PLAY -> {
                            readSomething = true
                            val vector = readVector()
                            Log.i(OURTAG, "NORMAL_PLAY : $vector")
                            playAt(vector.y, vector.x)
                        }
                        JsonTypes.InGame.BOMB_PLAY -> {
                            readSomething = true
                            val vector = readVector()
                            Log.i(OURTAG, "BOMB_PLAY : $vector")
                            playBomb(vector.y, vector.x)
                        }
                        JsonTypes.InGame.TRADE_PLAY -> {
                            readSomething = true
                            val tradePieces = ArrayList<Vector>()
                            jsonReader.beginArray()
                            for (i in 1..3) {
                                tradePieces.add(readVector())
                            }
                            jsonReader.endArray()
                            Log.i(OURTAG,
                                "TRADE_PLAY : ${tradePieces[0]}, ${tradePieces[1]}, ${tradePieces[2]}")
                            playTrade(tradePieces)
                        }
                        JsonTypes.InGame.PLAYER_PASSED -> {
                            Log.i(OURTAG, "received PLAYER_PASSED")
                            passPlayer()
                        }
                        JsonTypes.InGame.PLAYER_LEFT -> {
                            Log.i(OURTAG, "received PLAYER_LEFT")
                        }
                        JsonTypes.InGame.PLAYER_READY -> {
                            if (!alreadyReceivedReady) {
                                alreadyReceivedReady = true
                                ready()
                            }
                            Log.i(OURTAG, "received PLAYER_READY")
                        }
                        else -> {
                            Log.e(OURTAG,
                                "Received something that shouldn't have on GameSetupHostSide: $type")
                        }
                    }
                    if (!readSomething) jsonReader.nextNull()
                    endRead()
                }
            } catch (e: InterruptedException) {
                Log.i(OURTAG, "InterruptedException correu na thread GameSetupHostSide")
                shouldExit = true
                throw e
            } catch (e: Exception) {
                connectionsWelcomer.playerLeft(ownPlayer)
                Log.e(OURTAG, "", e)
            }
        }

    }

    //These functions get called from inside the thread loop
    override fun playAt(line: Int, column: Int) {
        game.playAt(getOwnPlayer(), line, column)
    }

    override fun playBomb(line: Int, column: Int) {
        game.playBombPiece(getOwnPlayer(), line, column)
    }

    override fun playTrade(tradePieces: ArrayList<Vector>) {
        game.playTrade(getOwnPlayer(), tradePieces)
    }

    override fun ready() {
        game.playerReady(getOwnPlayer())
    }

    override fun detach() {
        //TODO player saiu
    }

    override fun passPlayer() {
        game.passPlayer(getOwnPlayer())
    }
    //-------------------------------------------------------

    override fun isOnline() = true
    override fun getPlayers() = game.players
    override fun getCurrentPlayer() = game.currentPlayer

    override fun getOwnPlayer() = ownPlayer
    override fun getGameBoard() = game.board
    override fun getPossibleMoves() = game.currentPlayerPossibleMoves

    //The game calls these functions and we need to send it over to the other device
    override var possibleMovesCallback: ((List<Vector>) -> Unit)? = { moves ->
        queueAction {
            beginSendWithType(JsonTypes.InGame.POSSIBLE_MOVES)
            jsonWriter.beginArray()
            moves.forEach { vector ->
                writeVector(vector)
            }
            jsonWriter.endArray()
            Log.i(OURTAG, "send POSSIBLE_MOVES")
            endSend()
        }
    }
    override var updateBoardCallback: ((Array<Array<Piece>>) -> Unit)? = { board ->
        queueAction {
            beginSendWithType(JsonTypes.InGame.BOARD_CHANGED)
            writeBoardArray(board)
            Log.i(OURTAG, "send BOARD_CHANGED")
            endSend()
        }
    }
    override var changePlayerCallback: ((Int) -> Unit)? = { playerId ->
        queueAction {
            beginSendWithType(JsonTypes.InGame.PLAYER_CHANGED)
            jsonWriter.value(playerId)
            Log.i(OURTAG, "send PLAYER_CHANGED")
            endSend()
        }
    }
    override var gameFinishedCallback: ((GameEndStats) -> Unit)? = {
        queueAction {
            beginSendWithType(JsonTypes.InGame.GAME_FINISHED)
            jsonWriter.beginArray()
            it.playerStats.forEach {
                jsonWriter.beginObject()
                jsonWriter.name("playerId").value(it.player.playerId)
                jsonWriter.name("score").value(it.pieces)
                jsonWriter.endObject()
            }
            jsonWriter.endArray()
            Log.i(OURTAG, "send GAME_FINISHED")
            endSend()
        }
    }
    override var playerUsedBombCallback: ((Int) -> Unit)? = { pId ->
        queueAction {
            beginSendWithType(JsonTypes.InGame.PLAYER_USED_BOMB)
            jsonWriter.value(pId)
            Log.i(OURTAG, "send PLAYER_USED_BOMB")
            endSend()
        }
    }
    override var playerUsedTradeCallback: ((Int) -> Unit)? = { pId ->
        queueAction {
            beginSendWithType(JsonTypes.InGame.PLAYER_USED_TRADE)
            jsonWriter.value(pId)
            Log.i(OURTAG, "send PLAYER_USED_TRADE")
            endSend()
        }
    }

    override fun arrivedPlayer(player: Player) {
        queueAction {
            beginSendWithType(JsonTypes.Setup.NEW_PLAYER)
            writePlayer(player)
            endSend()
        }
    }

    override fun leftPayer(playerId: Int) {
        queueAction {
            beginSendWithType(JsonTypes.Setup.LEFT_PLAYER)
            jsonWriter.value(playerId)
            endSend()
        }
    }

    override fun sendExit() {
        queueAction {
            beginSendWithType(JsonTypes.Setup.EXITING)
            jsonWriter.nullValue()
            endSend()
            close()
        }
    }

    override fun sendStart(game: Game) {
        queueAction {
            beginSendWithType(JsonTypes.Setup.STARTING)
            writeStartingInformation(game)
            endSend()
            Log.i(OURTAG, "Send STARTING correu")
        }
    }
}