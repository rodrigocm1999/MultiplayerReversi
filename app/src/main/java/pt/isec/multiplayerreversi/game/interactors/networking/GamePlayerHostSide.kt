package pt.isec.multiplayerreversi.game.interactors.networking

import android.util.Log
import pt.isec.multiplayerreversi.App.Companion.OURTAG
import pt.isec.multiplayerreversi.game.interactors.GamePlayer
import pt.isec.multiplayerreversi.game.interactors.JsonTypes
import pt.isec.multiplayerreversi.game.logic.*
import java.lang.Exception
import java.net.Socket

class GamePlayerHostSide(
    private val game: Game,
    private val ownPlayer: Player,
    socket: Socket,
) : AbstractNetworkingProxy(socket), GamePlayer {

    private var shouldExit = false

    init {
        addThread {
            try {
                while (!shouldExit) {
                    //TODO 0 find out why this is not receiving anything
                    val type = beginReadAndGetType()
                    var readSomething = false
                    when (type) {
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
                            for (i in 1..3) {
                                tradePieces.add(readVector())
                            }
                            Log.i(OURTAG,
                                "TRADE_PLAY : ${tradePieces[0]}, ${tradePieces[1]}, ${tradePieces[2]}")
                            playTrade(tradePieces)
                        }
                        JsonTypes.InGame.PLAYER_PASSED -> {
                            Log.i(OURTAG, "PLAYER_PASSED")
                            passPlayer()
                        }
                        JsonTypes.InGame.PLAYER_LEFT -> {
                            Log.i(OURTAG, "PLAYER_LEFT")
                        }
                        JsonTypes.InGame.PLAYER_READY -> {
                            Log.i(OURTAG, "PLAYER_READY")
                            ready()
                        }
                        else -> {
                            Log.e(OURTAG,"Received something that shouldn't have" +
                                    " on GameSetupRemoteSide: $type")
                        }
                    }
                    if (!readSomething) jsonReader.nextNull()
                    jsonReader.endObject()
                }
            } catch (e: InterruptedException) {
                endRead()
                throw e
            } catch (e: Exception) {
                throw e
            }
        }

        addThread {
            while (!shouldExit) {
                val action = queuedActions.take()
                action()
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
}