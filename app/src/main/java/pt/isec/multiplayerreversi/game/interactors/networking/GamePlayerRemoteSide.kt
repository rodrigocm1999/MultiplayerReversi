package pt.isec.multiplayerreversi.game.interactors.networking

import android.util.Log
import pt.isec.multiplayerreversi.App
import pt.isec.multiplayerreversi.App.Companion.OURTAG
import pt.isec.multiplayerreversi.game.interactors.GamePlayer
import pt.isec.multiplayerreversi.game.interactors.JsonTypes
import pt.isec.multiplayerreversi.game.logic.*
import java.io.IOException
import java.net.Socket
import kotlin.collections.ArrayList
import kotlin.concurrent.thread

class GamePlayerRemoteSide(
    socket: Socket, profile: Profile,
    override val arrivedPlayerCallback: ((Player) -> Unit),
    override val leftPlayerCallback: (Player) -> Unit,
    override val hostExitedCallback: (() -> Unit),
    override val gameStartingCallback: ((GamePlayer) -> Unit),
) : AbstractNetworkingProxy(socket), GamePlayer, IGameSetupRemoteSide {

    private var gameData: GameData
    private var ownPlayer: Player = Player(profile)

    init {
        val settings = App.GameSettings()
        receiveThrough { _, jsonReader ->
            jsonReader.beginObject()
            while (jsonReader.hasNext()) {
                when (jsonReader.nextName()) {
                    "showPossibleMoves" -> settings.showPossibleMoves = jsonReader.nextBoolean()
                    else -> {
                        Log.e(OURTAG, "Invalid gameSettings received")
                        jsonReader.skipValue()
                    }
                }
            }
            jsonReader.endObject()
            return@receiveThrough true
        }

        val players = ArrayList<Player>(3)
        receiveThrough { _, jsonReader ->
            players.addAll(jsonReader.readPlayers())
            players.add(ownPlayer)
            return@receiveThrough true
        }

        sendThrough(JsonTypes.Setup.SEND_PROFILE) { jsonWriter ->
            jsonWriter.writeProfile(profile)
        }

        gameData = GameData(settings, players)

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
                    JsonTypes.Setup.PLAYER_LEFT_WAITING_ROOM -> {
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
                    JsonTypes.Setup.HOST_EXITING -> {
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
                        jsonReader.beginObject()
                        while (jsonReader.hasNext()) {
                            when (jsonReader.nextName()) {
                                "board" -> jsonReader.readBoardArray(gameData.board)
                                "scores" -> jsonReader.readScoresArray(gameData.players)
                            }
                        }
                        jsonReader.endObject()
                        Log.i(OURTAG, "received BOARD_CHANGED")
                        updateBoardCallback?.invoke(gameData.board)
                        return@setReceiving true
                    }
                    JsonTypes.InGame.PLAYER_CHANGED -> {
                        val playerId = jsonReader.nextInt()
                        Log.i(OURTAG, "received PLAYER_CHANGED : $playerId")
                        gameData.currentPlayer = getPlayerById(playerId)!!
                        changePlayerCallback?.invoke(playerId)
                        return@setReceiving true
                    }
                    JsonTypes.InGame.PLAYER_USED_BOMB -> {
                        val playerId = jsonReader.nextInt()
                        Log.i(OURTAG, "received PLAYER_USED_BOMB : $playerId")
                        getPlayerById(playerId)!!.useBomb()
                        playerUsedBombCallback?.invoke(playerId)
                        return@setReceiving true
                    }
                    JsonTypes.InGame.PLAYER_USED_TRADE -> {
                        val playerId = jsonReader.nextInt()
                        Log.i(OURTAG, "received PLAYER_USED_TRADE : $playerId")
                        getPlayerById(playerId)!!.useTrade()
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
                            val p = getPlayerById(pId)!!
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
                    JsonTypes.InGame.GAME_TERMINATED -> {
                        Log.i(OURTAG, "received GAME_TERMINATED")
                        gameTerminatedCallback?.let { it() }
                    }
                    JsonTypes.Setup.DATA -> {}
                    else -> {
                        Log.e(OURTAG,
                            "Received something that shouldn't have on GameSetupRemoteSide: $type")
                        return@setReceiving false
                    }
                }
                return@setReceiving false
            } catch (e: IOException) {
                shouldExit = true
                gameTerminatedCallback?.let { it() }
                Log.e(OURTAG, "Error socket exception", e)
            }
            return@setReceiving false
        }
    }


    override fun playAt(line: Int, column: Int) {
        queueJsonWrite(JsonTypes.InGame.NORMAL_PLAY) { jsonWriter ->
            val v = Vector(column, line)
            jsonWriter.writeVector(v)
        }
    }

    override fun playBomb(line: Int, column: Int) {
        queueJsonWrite(JsonTypes.InGame.BOMB_PLAY) { jsonWriter ->
            val v = Vector(column, line)
            jsonWriter.writeVector(v)
        }
    }

    override fun playTrade(tradePieces: ArrayList<Vector>) {
        queueJsonWrite(JsonTypes.InGame.TRADE_PLAY) { jsonWriter ->
            jsonWriter.beginArray()
            tradePieces.forEach { jsonWriter.writeVector(it) }
            jsonWriter.endArray()
        }
    }

    override fun ready() {
        queueJsonWrite(JsonTypes.InGame.PLAYER_DEVICE_READY) { jsonWriter ->
            jsonWriter.nullValue()
        }
    }

    override fun leaveGame() {
        queueJsonWrite(JsonTypes.InGame.PLAYER_LEFT_RUNNING_GAME) { jsonWriter ->
            jsonWriter.nullValue()
        }
        queueClose()
    }

    override fun passPlayer() {
        queueJsonWrite(JsonTypes.InGame.PLAYER_PASSED_TURN) { jsonWriter ->
            jsonWriter.nullValue()
        }
    }

    override fun leaveWaitingArea() {
        queueJsonWrite(JsonTypes.Setup.PLAYER_LEFT_WAITING_ROOM) { jsonWriter ->
            jsonWriter.value(ownPlayer.playerId)
        }
        queueClose()
    }


    override fun isOnline() = true
    override fun getPlayers() = gameData.players
    override fun getCurrentPlayer() = gameData.currentPlayer

    override fun getOwnPlayer() = ownPlayer
    override fun getGameData() = gameData

    override fun getGameBoard() = gameData.board
    override fun getPossibleMoves() = gameData.currentPlayerPossibleMoves

    override var possibleMovesCallback: ((List<Vector>) -> Unit)? = null
    override var updateBoardCallback: ((Array<Array<Piece>>) -> Unit)? = null
    override var changePlayerCallback: ((Int) -> Unit)? = null
    override var gameFinishedCallback: ((GameEndStats) -> Unit)? = null
    override var playerUsedBombCallback: ((Int) -> Unit)? = null
    override var playerUsedTradeCallback: ((Int) -> Unit)? = null
    override var gameTerminatedCallback: (() -> Unit)? = null
}