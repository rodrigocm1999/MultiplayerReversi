package pt.isec.multiplayerreversi.game.interactors.networking

import android.util.Log
import pt.isec.multiplayerreversi.App
import pt.isec.multiplayerreversi.App.Companion.OURTAG
import pt.isec.multiplayerreversi.game.interactors.GamePlayer
import pt.isec.multiplayerreversi.game.interactors.JsonTypes
import pt.isec.multiplayerreversi.game.logic.*
import java.lang.Exception
import java.net.Socket

class GamePlayerHostSide(
    app: App,
    socket: Socket,
    connectionsWelcomer: ConnectionsWelcomer,
    override val readyUpCallback: (Int) -> Unit,
) : AbstractNetworkingProxy(socket), GamePlayer, IGameSetupHostSide {

    private lateinit var ownPlayer: Player
    lateinit var game: Game

    private var alreadyReceivedReady = false

    init {
        val gameSettings = app.sharedGamePreferences
        sendThrough(JsonTypes.Setup.SETTINGS) {
            it.beginObject()
            it.name("showPossibleMoves").value(gameSettings.showPossibleMoves)
            it.endObject()
        }

        sendThrough(JsonTypes.Setup.PLAYERS) {
            it.writePlayers(connectionsWelcomer.getPlayers())
        }

        receiveThrough { _, jsonReader ->
            ownPlayer = Player()
            jsonReader.readPlayer(ownPlayer)
            connectionsWelcomer.joinPlayer(ownPlayer, this)
            return@receiveThrough true
        }

        sendThrough(JsonTypes.Setup.PLAYER_IDS) {
            it.writePlayerIds(ownPlayer.playerId, ownPlayer.piece)
        }

        setReceiving("GameSetupHostSide receive") { type, jsonReader ->
            try {
                when (type) {
                    JsonTypes.Setup.LEFT_PLAYER -> {
                        connectionsWelcomer.playerLeft(ownPlayer)
                    }
                    JsonTypes.InGame.NORMAL_PLAY -> {
                        val vector = jsonReader.readVector()
                        Log.i(OURTAG, "NORMAL_PLAY : $vector")
                        playAt(vector.y, vector.x)
                        return@setReceiving true
                    }
                    JsonTypes.InGame.BOMB_PLAY -> {
                        val vector = jsonReader.readVector()
                        Log.i(OURTAG, "BOMB_PLAY : $vector")
                        playBomb(vector.y, vector.x)
                        return@setReceiving true
                    }
                    JsonTypes.InGame.TRADE_PLAY -> {
                        val tradePieces = ArrayList<Vector>()
                        jsonReader.beginArray()
                        for (i in 1..3) {
                            tradePieces.add(jsonReader.readVector())
                        }
                        jsonReader.endArray()
                        Log.i(OURTAG,
                            "TRADE_PLAY : ${tradePieces[0]}, ${tradePieces[1]}, ${tradePieces[2]}")
                        playTrade(tradePieces)
                        return@setReceiving true
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
            } catch (e: InterruptedException) {
                Log.i(OURTAG, "InterruptedException correu na thread GameSetupHostSide")
                shouldExit = true
                throw e
            } catch (e: Exception) {
                connectionsWelcomer.playerLeft(ownPlayer)
                Log.e(OURTAG, "", e)
            }
            return@setReceiving false
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
        writeJson(JsonTypes.InGame.POSSIBLE_MOVES) { jsonWriter ->
            jsonWriter.beginArray()
            moves.forEach { vector ->
                jsonWriter.writeVector(vector)
            }
            jsonWriter.endArray()
            Log.i(OURTAG, "send POSSIBLE_MOVES")
        }
    }
    override var updateBoardCallback: ((Array<Array<Piece>>) -> Unit)? = { board ->
        writeJson(JsonTypes.InGame.BOARD_CHANGED) { jsonWriter ->
            jsonWriter.writeBoardArray(board)
            Log.i(OURTAG, "send BOARD_CHANGED")
        }
    }
    override var changePlayerCallback: ((Int) -> Unit)? = { playerId ->
        writeJson(JsonTypes.InGame.PLAYER_CHANGED) { jsonWriter ->
            jsonWriter.value(playerId)
            Log.i(OURTAG, "send PLAYER_CHANGED")
        }
    }
    override var gameFinishedCallback: ((GameEndStats) -> Unit)? = {
        writeJson(JsonTypes.InGame.GAME_FINISHED) { jsonWriter ->
            jsonWriter.beginArray()
            it.playerStats.forEach {
                jsonWriter.beginObject()
                jsonWriter.name("playerId").value(it.player.playerId)
                jsonWriter.name("score").value(it.pieces)
                jsonWriter.endObject()
            }
            jsonWriter.endArray()
            Log.i(OURTAG, "send GAME_FINISHED")
        }
    }
    override var playerUsedBombCallback: ((Int) -> Unit)? = { pId ->
        writeJson(JsonTypes.InGame.PLAYER_USED_BOMB) { jsonWriter ->
            jsonWriter.value(pId)
            Log.i(OURTAG, "send PLAYER_USED_BOMB")
        }
    }
    override var playerUsedTradeCallback: ((Int) -> Unit)? = { pId ->
        writeJson(JsonTypes.InGame.PLAYER_USED_TRADE) { jsonWriter ->
            jsonWriter.value(pId)
            Log.i(OURTAG, "send PLAYER_USED_TRADE")
        }
    }

    override fun arrivedPlayer(player: Player) {
        writeJson(JsonTypes.InGame.PLAYER_USED_TRADE) { jsonWriter ->
            jsonWriter.writePlayer(player)
        }
    }

    override fun leftPayer(playerId: Int) {
        writeJson(JsonTypes.Setup.LEFT_PLAYER) { jsonWriter ->
            jsonWriter.value(playerId)
        }
    }

    override fun sendExit() {
        writeJson(JsonTypes.Setup.EXITING) { jsonWriter ->
            jsonWriter.nullValue()
            close()
        }
    }

    override fun sendStart(game: Game) {
        writeJson(JsonTypes.Setup.STARTING) { jsonWriter ->
            jsonWriter.writeStartingInformation(game)
            Log.i(OURTAG, "Send STARTING correu")
        }
    }
}