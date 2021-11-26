package pt.isec.multiplayerreversi.game.interactors.networking

import android.util.Log
import pt.isec.multiplayerreversi.App.Companion.OURTAG
import pt.isec.multiplayerreversi.game.interactors.GamePlayer
import pt.isec.multiplayerreversi.game.interactors.JsonTypes
import pt.isec.multiplayerreversi.game.logic.*
import pt.isec.multiplayerreversi.game.logic.Vector
import java.net.Socket
import java.util.*
import kotlin.concurrent.thread

class GamePlayerRemoteSide(
    socket: Socket,
    private val gameData: GameData,
    private val ownPlayer: Player,
) :
    AbstractNetworkingSetupProxy(socket), GamePlayer {

    private var shouldExit = false

    init {
        thread {
            while (!shouldExit) {
                try {
                    val type = beginReadAndGetType()
                    var readSomething = false
                    when (type) {
                        JsonTypes.InGame.BOARD_CHANGED -> {
                            readBoardArray(gameData.board)
                            readSomething = true
                            Log.i(OURTAG, "Read Board ${gameData.board}")
                            updateBoardCallback?.invoke(gameData.board)
                        }
                        JsonTypes.InGame.PLAYER_CHANGED -> {
                            val pId = jsonReader.nextInt()
                            readSomething = true
                            Log.i(OURTAG, "Player changed")
                            changePlayerCallback?.invoke(pId)
                        }
                        JsonTypes.InGame.PLAYER_USED_BOMB -> {
                            val playerId = jsonReader.nextInt()
                            readSomething = true
                            playerUsedBombCallback?.invoke(playerId)
                        }
                        JsonTypes.InGame.PLAYER_USED_TRADE -> {
                            val playerId = jsonReader.nextInt()
                            readSomething = true
                            playerUsedTradeCallback?.invoke(playerId)
                        }
                        JsonTypes.InGame.GAME_FINISHED -> {
                            //TODO read the shits
//                        gameFinishedCallback?.invoke(gameStats)
                            shouldExit = true
                        }
                        else -> {
                            Log.i(OURTAG,
                                "Received something ILLEGAL on GamePlayerRemoteSide socket loop : $type")
                        }
                    }
                    Log.i(OURTAG, "Received $type GamePlayerRemoteSide socket loop")
                    if (!readSomething) jsonReader.nextNull()
                    jsonReader.endObject()
                } catch (e: Exception) {
                    Log.e(OURTAG, "", e)
                }
            }
        }
    }

    override fun playAt(line: Int, column: Int) {
        queueAction {
            beginSendWithType(JsonTypes.InGame.NORMAL_PLAY)
            jsonWriter.value(getOwnPlayer().playerId)
            endSend()
        }
    }

    override fun playBomb(line: Int, column: Int) {
        queueAction {
            beginSendWithType(JsonTypes.InGame.BOMB_PLAY)
            jsonWriter.value(getOwnPlayer().playerId)
            endSend()
        }
    }

    override fun playTrade(tradePieces: ArrayList<Vector>) {
        queueAction {
            beginSendWithType(JsonTypes.InGame.TRADE_PLAY)
            jsonWriter.value(getOwnPlayer().playerId)
            endSend()
        }
    }

    override fun ready() {
        queueAction {
            beginSendWithType(JsonTypes.InGame.PLAYER_READY)
            jsonWriter.value(getOwnPlayer().playerId)
            endSend()
        }
    }

    override fun detach() {
        queueAction {
            beginSendWithType(JsonTypes.InGame.PLAYER_LEFT)
            jsonWriter.value(getOwnPlayer().playerId)
            endSend()
        }
    }

    override fun isOnline() = true
    override fun getPlayers() = gameData.players
    override fun getOwnPlayer() = ownPlayer
    override fun getGameBoard() = gameData.board
    override fun getPossibleMoves() = gameData.currentPlayerPossibleMoves

    override var possibleMovesCallback: ((List<Vector>) -> Unit)? = null
    override var updateBoardCallback: ((Array<Array<Piece>>) -> Unit)? = null
    override var changePlayerCallback: ((Int) -> Unit)? = null
    override var gameFinishedCallback: ((GameEndStats) -> Unit)? = null
    override var playerUsedBombCallback: ((Int) -> Unit)? = null
    override var playerUsedTradeCallback: ((Int) -> Unit)? = null
}