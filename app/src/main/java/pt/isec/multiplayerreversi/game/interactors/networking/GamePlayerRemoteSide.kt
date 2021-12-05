package pt.isec.multiplayerreversi.game.interactors.networking

import android.util.Log
import pt.isec.multiplayerreversi.App.Companion.OURTAG
import pt.isec.multiplayerreversi.game.interactors.GamePlayer
import pt.isec.multiplayerreversi.game.interactors.JsonTypes
import pt.isec.multiplayerreversi.game.logic.*
import pt.isec.multiplayerreversi.game.logic.Vector
import java.net.Socket
import java.net.SocketException
import kotlin.collections.ArrayList
import kotlin.concurrent.thread

class GamePlayerRemoteSide(
    socket: Socket,
    profile: Profile,
    override val arrivedPlayerCallback: ((Player) -> Unit),
    override val leftPlayerCallback: (Player) -> Unit,
    override val hostExitedCallback: (() -> Unit),
    override val gameStartingCallback: ((GamePlayer) -> Unit),
) :
    AbstractNetworkingProxy(socket), GamePlayer, IGameSetupRemoteSide {

    private val gameData = GameData()
    private var ownPlayer: Player = Player(profile)

    init {
        beginRead()
        gameData.players = ArrayList()
        gameData.players.addAll(readPlayers())
        gameData.players.add(ownPlayer)
        endRead()

        beginSend()
        writeProfile(profile)
        endSend()

        //Player object gets its fields filled up
        beginRead()
        readPlayerIds(ownPlayer)
        endRead()

        println(ownPlayer)

        setReceiving("GamePlayerRemoteSide send") {
            while (!shouldExit) {
                try {
                    val type = beginReadAndGetType()
                    var readSomething = false
                    when (type) {
                        JsonTypes.Setup.NEW_PLAYER -> {
                            Log.i(OURTAG, "received NEW_PLAYER")
                            val p = Player()
                            readPlayer(p)
                            readSomething = true
                            gameData.players.add(p)
                            arrivedPlayerCallback(p)
                        }
                        JsonTypes.Setup.LEFT_PLAYER -> {
                            Log.i(OURTAG, "received LEFT_PLAYER")
                            val p = Player()
                            val playerId = jsonReader.nextInt()
                            readSomething = true
                            for (i in 0 until gameData.players.size)
                                if (gameData.players[i].playerId == playerId) {
                                    gameData.players.removeAt(i)
                                    break
                                }
                            leftPlayerCallback(p)
                        }
                        JsonTypes.Setup.STARTING -> {
                            Log.i(OURTAG, "received STARTING")
                            readStartingInformation(gameData)
                            readSomething = true
                            ownPlayer.callbacks = this
                            thread {
                                gameStartingCallback(this)
                            }
                        }
                        JsonTypes.Setup.EXITING -> {
                            Log.i(OURTAG, "received EXITING")
                            hostExitedCallback()
                        }
                        JsonTypes.InGame.POSSIBLE_MOVES -> {
                            Log.i(OURTAG, "received POSSIBLE_MOVES~-----------~-----------~-----------~-----------~-----------")
                            val possibleMoves = ArrayList<Vector>()
                            jsonReader.beginArray()
                            while (jsonReader.hasNext()) {
                                possibleMoves.add(readVector())
                            }
                            jsonReader.endArray()
                            readSomething = true
                            gameData.currentPlayerPossibleMoves = possibleMoves
                            Log.i(OURTAG, "received POSSIBLE_MOVES")
                            possibleMovesCallback?.invoke(possibleMoves)
                        }
                        JsonTypes.InGame.BOARD_CHANGED -> {
                            readBoardArray(gameData.board)
                            readSomething = true
                            Log.i(OURTAG, "received BOARD_CHANGED")
                            updateBoardCallback?.invoke(gameData.board)
                        }
                        JsonTypes.InGame.PLAYER_CHANGED -> {
                            val playerId = jsonReader.nextInt()
                            readSomething = true
                            Log.i(OURTAG, "received PLAYER_CHANGED : $playerId")
                            gameData.currentPlayer = gameData.getPlayer(playerId)!!
                            changePlayerCallback?.invoke(playerId)
                        }
                        JsonTypes.InGame.PLAYER_USED_BOMB -> {
                            val playerId = jsonReader.nextInt()
                            readSomething = true
                            Log.i(OURTAG, "received PLAYER_USED_BOMB : $playerId")
                            gameData.getPlayer(playerId)!!.hasUsedBomb = true
                            playerUsedBombCallback?.invoke(playerId)
                        }
                        JsonTypes.InGame.PLAYER_USED_TRADE -> {
                            val playerId = jsonReader.nextInt()
                            readSomething = true
                            Log.i(OURTAG, "received PLAYER_USED_TRADE : $playerId")
                            gameData.getPlayer(playerId)!!.hasUsedTrade = true
                            playerUsedTradeCallback?.invoke(playerId)
                        }
                        JsonTypes.InGame.GAME_FINISHED -> {
                            val playersStats = ArrayList<PlayerEndStats>()
                            var highestScore = -1
                            var highestPlayerScoreId = -1
                            jsonReader.beginArray()
                            while (jsonReader.hasNext()) {
                                jsonReader.beginObject()

                                var score = -1
                                var pId = -1
                                while (jsonReader.hasNext()) {
                                    when (jsonReader.nextName()) {
                                        "playerId" -> pId = jsonReader.nextInt()
                                        "score" -> score = jsonReader.nextInt()
                                    }
                                }
                                val p = getPlayers().find { it.playerId == pId }!!
                                playersStats.add(PlayerEndStats(p, score))

                                if (score > highestScore) {
                                    highestPlayerScoreId = p.playerId
                                    highestScore = score
                                } else if (score == highestScore)
                                    highestPlayerScoreId = -1

                                jsonReader.endObject()
                            }
                            jsonWriter.endArray()
                            readSomething = true

                            val gameEndStats = GameEndStats(highestPlayerScoreId, playersStats)
                            gameFinishedCallback?.invoke(gameEndStats)
                            shouldExit = true
                            Log.i(OURTAG, "received GAME_FINISHED : $gameEndStats")
                        }
                        else -> {
                            Log.e(OURTAG,
                                "Received something that shouldn't have on GameSetupRemoteSide: $type")
                        }
                    }
                    if (!readSomething) jsonReader.nextNull()
                    endRead()
                } catch (e: InterruptedException) {
                    Log.i(OURTAG, "InterruptedException correu na thread GameSetupRemoteSide")
                    shouldExit = true
                    throw e
                } catch (e: SocketException) {
                    //TODO handle errors, ask if want to continue locally or terminate
                    break
                }
            } // while
        }
    }

    override fun playAt(line: Int, column: Int) {
        queueAction {
            beginSendWithType(JsonTypes.InGame.NORMAL_PLAY)
            val v = Vector(column, line)
            writeVector(v)
            Log.i(OURTAG, "send NORMAL_PLAY : $v")
            endSend()
        }
    }

    override fun playBomb(line: Int, column: Int) {
        queueAction {
            beginSendWithType(JsonTypes.InGame.BOMB_PLAY)
            val v = Vector(column, line)
            writeVector(v)
            Log.i(OURTAG, "send BOMB_PLAY : $v")
            endSend()
        }
    }

    override fun playTrade(tradePieces: ArrayList<Vector>) {
        queueAction {
            beginSendWithType(JsonTypes.InGame.TRADE_PLAY)
            jsonWriter.beginArray()
            tradePieces.forEach {
                writeVector(it)
            }
            jsonWriter.endArray()
            Log.i(OURTAG,
                "send TRADE_PLAY : ${tradePieces[0]}, ${tradePieces[1]}, ${tradePieces[2]}")
            endSend()
        }
    }

    override fun ready() {
        queueAction {
            beginSendWithType(JsonTypes.InGame.PLAYER_READY)
            jsonWriter.nullValue()
            Log.i(OURTAG, "send PLAYER_READY")
            endSend()
        }
    }

    override fun detach() {
        queueAction {
            beginSendWithType(JsonTypes.InGame.PLAYER_LEFT)
            jsonWriter.nullValue()
            Log.i(OURTAG, "send PLAYER_LEFT")
            endSend()

            this.close()
        }
    }

    override fun passPlayer() {
        queueAction {
            beginSendWithType(JsonTypes.InGame.PLAYER_PASSED)
            jsonWriter.nullValue()
            Log.i(OURTAG, "send PLAYER_PASSED")
            endSend()
        }
    }

    override fun leave() {
        queueAction {
            beginSendWithType(JsonTypes.Setup.LEFT_PLAYER)
            jsonWriter.value(ownPlayer.playerId)
            endSend()
        }
    }


    override fun isOnline() = true
    override fun getPlayers() = gameData.players
    override fun getCurrentPlayer() = gameData.currentPlayer

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