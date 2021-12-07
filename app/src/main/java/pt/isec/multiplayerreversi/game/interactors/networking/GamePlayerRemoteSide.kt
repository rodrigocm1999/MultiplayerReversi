package pt.isec.multiplayerreversi.game.interactors.networking

import android.util.Log
import pt.isec.multiplayerreversi.App.Companion.OURTAG
import pt.isec.multiplayerreversi.game.interactors.GamePlayer
import pt.isec.multiplayerreversi.game.interactors.JsonTypes
import pt.isec.multiplayerreversi.game.logic.*
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
) : AbstractNetworkingProxy(socket), GamePlayer, IGameSetupRemoteSide {

    private val gameData = GameData()
    private var ownPlayer: Player = Player(profile)

    init {
        receiveThrough { _, jsonReader ->
            gameData.players = ArrayList()
            gameData.players.addAll(jsonReader.readPlayers())
            gameData.players.add(ownPlayer)
            return@receiveThrough true
        }

        sendThrough(JsonTypes.Setup.SEND_PROFILE) { jsonWriter ->
            jsonWriter.writeProfile(profile)
        }

        receiveThrough { _, jsonReader ->
            //Player object gets its fields filled up
            jsonReader.readPlayerIds(ownPlayer)
            return@receiveThrough true
        }
        println(ownPlayer)

        setReceiving("GamePlayerRemoteSide send") { type, jsonReader ->
            try {
                when (type) {
                    JsonTypes.Setup.NEW_PLAYER -> {
                        Log.i(OURTAG, "received NEW_PLAYER")
                        val p = Player()
                        jsonReader.readPlayer(p)
                        gameData.players.add(p)
                        arrivedPlayerCallback(p)
                        return@setReceiving true
                    }
                    JsonTypes.Setup.LEFT_PLAYER -> {
                        Log.i(OURTAG, "received LEFT_PLAYER")
                        val p = Player()
                        val playerId = jsonReader.nextInt()
                        for (i in 0 until gameData.players.size)
                            if (gameData.players[i].playerId == playerId) {
                                gameData.players.removeAt(i)
                                break
                            }
                        leftPlayerCallback(p)
                        return@setReceiving true
                    }
                    JsonTypes.Setup.STARTING -> {
                        Log.i(OURTAG, "received STARTING")
                        jsonReader.readStartingInformation(gameData)
                        ownPlayer.callbacks = this
                        thread {
                            gameStartingCallback(this)
                        }
                        return@setReceiving true
                    }
                    JsonTypes.Setup.EXITING -> {
                        Log.i(OURTAG, "received EXITING")
                        hostExitedCallback()
                    }
                    JsonTypes.InGame.POSSIBLE_MOVES -> {
                        val possibleMoves = ArrayList<Vector>()
                        jsonReader.beginArray()
                        while (jsonReader.hasNext()) {
                            possibleMoves.add(jsonReader.readVector())
                        }
                        jsonReader.endArray()
                        gameData.currentPlayerPossibleMoves = possibleMoves
                        Log.i(OURTAG, "received POSSIBLE_MOVES")
                        possibleMovesCallback?.invoke(possibleMoves)
                        return@setReceiving true
                    }
                    JsonTypes.InGame.BOARD_CHANGED -> {
                        jsonReader.readBoardArray(gameData.board)
                        Log.i(OURTAG, "received BOARD_CHANGED")
                        updateBoardCallback?.invoke(gameData.board)
                        return@setReceiving true
                    }
                    JsonTypes.InGame.PLAYER_CHANGED -> {
                        val playerId = jsonReader.nextInt()
                        Log.i(OURTAG, "received PLAYER_CHANGED : $playerId")
                        gameData.currentPlayer = gameData.getPlayer(playerId)!!
                        changePlayerCallback?.invoke(playerId)
                        return@setReceiving true
                    }
                    JsonTypes.InGame.PLAYER_USED_BOMB -> {
                        val playerId = jsonReader.nextInt()
                        Log.i(OURTAG, "received PLAYER_USED_BOMB : $playerId")
                        gameData.getPlayer(playerId)!!.useBomb()
                        playerUsedBombCallback?.invoke(playerId)
                        return@setReceiving true
                    }
                    JsonTypes.InGame.PLAYER_USED_TRADE -> {
                        val playerId = jsonReader.nextInt()
                        Log.i(OURTAG, "received PLAYER_USED_TRADE : $playerId")
                        gameData.getPlayer(playerId)!!.useTrade()
                        playerUsedTradeCallback?.invoke(playerId)
                        return@setReceiving true
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
                        jsonReader.endArray()

                        val gameEndStats = GameEndStats(highestPlayerScoreId, playersStats)
                        gameFinishedCallback?.invoke(gameEndStats)
                        shouldExit = true
                        Log.i(OURTAG, "received GAME_FINISHED : $gameEndStats")
                        return@setReceiving true
                    }
                    else -> {
                        Log.e(OURTAG,
                            "Received something that shouldn't have on GameSetupRemoteSide: $type")
                        return@setReceiving false
                    }
                }
                return@setReceiving false
            } catch (e: InterruptedException) {
                Log.i(OURTAG, "InterruptedException correu na thread GameSetupRemoteSide", e)
                shouldExit = true
                throw e
            } catch (e: SocketException) {
                //TODO handle errors, ask if want to continue locally or terminate
                Log.e(OURTAG, "Error socket exception", e)
            }
            return@setReceiving false
        }
    }


    override fun playAt(line: Int, column: Int) {
        writeJson(JsonTypes.InGame.NORMAL_PLAY) { jsonWriter ->
            val v = Vector(column, line)
            jsonWriter.writeVector(v)
            Log.i(OURTAG, "send NORMAL_PLAY : $v")
        }
    }

    override fun playBomb(line: Int, column: Int) {
        writeJson(JsonTypes.InGame.BOMB_PLAY) { jsonWriter ->
            val v = Vector(column, line)
            jsonWriter.writeVector(v)
            Log.i(OURTAG, "send BOMB_PLAY : $v")
        }
    }

    override fun playTrade(tradePieces: ArrayList<Vector>) {
        writeJson(JsonTypes.InGame.TRADE_PLAY) { jsonWriter ->
            jsonWriter.beginArray()
            tradePieces.forEach {
                jsonWriter.writeVector(it)
            }
            jsonWriter.endArray()
            Log.i(OURTAG,
                "send TRADE_PLAY : ${tradePieces[0]}, ${tradePieces[1]}, ${tradePieces[2]}")
        }
    }

    override fun ready() {
        writeJson(JsonTypes.InGame.PLAYER_READY) { jsonWriter ->
            jsonWriter.nullValue()
            Log.i(OURTAG, "send PLAYER_READY")
        }
    }

    override fun detach() {
        writeJson(JsonTypes.InGame.PLAYER_LEFT) { jsonWriter ->
            jsonWriter.nullValue()
            Log.i(OURTAG, "send PLAYER_LEFT")

            this.close()
        }
    }

    override fun passPlayer() {
        writeJson(JsonTypes.InGame.PLAYER_PASSED) { jsonWriter ->
            jsonWriter.nullValue()
            Log.i(OURTAG, "send PLAYER_PASSED")
        }
    }

    override fun leave() {
        writeJson(JsonTypes.Setup.LEFT_PLAYER) { jsonWriter ->
            jsonWriter.value(ownPlayer.playerId)
            Log.i(OURTAG, "send LEFT_PLAYER")
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