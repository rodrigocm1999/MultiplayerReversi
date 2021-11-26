package pt.isec.multiplayerreversi.game.interactors.networking

import android.util.Log
import pt.isec.multiplayerreversi.App
import pt.isec.multiplayerreversi.game.interactors.GamePlayer
import pt.isec.multiplayerreversi.game.interactors.JsonTypes
import pt.isec.multiplayerreversi.game.logic.*
import pt.isec.multiplayerreversi.game.logic.Vector
import java.net.Socket
import java.util.*
import java.util.concurrent.ArrayBlockingQueue
import kotlin.concurrent.thread

class GamePlayerHostSide(
    private val game: Game,
    private val ownPlayer: Player,
    socket: Socket,
) : AbstractNetworkingSetupProxy(socket), GamePlayer {

    private var shouldExit = false

    init {
        addThread {
            while (!shouldExit) {
                val type = beginReadAndGetType()
                var readSomething = false
                when (type) {
                    JsonTypes.InGame.NORMAL_PLAY -> {
                        val vector = readVector()
                        readSomething = true
                        playAt(vector.y, vector.x)
                    }
                    JsonTypes.InGame.BOMB_PLAY -> {
                        val vector = readVector()
                        readSomething = true
                        playBomb(vector.y, vector.x)
                    }
                    JsonTypes.InGame.TRADE_PLAY -> {
                        val vector = readVector()
                        readSomething = true
                        playBomb(vector.y, vector.x)
                    }
                    //TODO 5 temos de fazer o pass ainda
                    else -> {
                        Log.e(App.OURTAG,
                            "Received something that shouldn't have on GameSetupRemoteSide: $type")
                    }
                }
                if (!readSomething) jsonReader.nextNull()
                jsonReader.endObject()
            }
        }

        addThread {
            while (!shouldExit) {
                val block = blockingQueue.take()
                block()
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

    override fun detach() {}
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
            endSend()
        }
    }
    override var updateBoardCallback: ((Array<Array<Piece>>) -> Unit)? = { board ->
        queueAction {
            beginSendWithType(JsonTypes.InGame.BOARD_CHANGED)
            writeBoardArray(board)
            endSend()
        }
    }
    override var changePlayerCallback: ((Int) -> Unit)? = { playerId ->
        queueAction {
            beginSendWithType(JsonTypes.InGame.PLAYER_CHANGED)
            jsonWriter.value(playerId)
            endSend()
        }
    }
    override var gameFinishedCallback: ((GameEndStats) -> Unit)? = {
        //TODO game finished end stats send over socket
        queueAction {
            beginSendWithType(JsonTypes.InGame.GAME_FINISHED)
            endSend()
        }
    }
    override var playerUsedBombCallback: ((Int) -> Unit)? = {
        queueAction {
            beginSendWithType(JsonTypes.InGame.PLAYER_USED_BOMB)
            jsonWriter.value(it)
            endSend()
        }
    }
    override var playerUsedTradeCallback: ((Int) -> Unit)? = {
        queueAction {
            beginSendWithType(JsonTypes.InGame.PLAYER_USED_TRADE)
            jsonWriter.value(it)
            endSend()
        }
    }
}