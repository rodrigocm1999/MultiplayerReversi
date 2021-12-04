package pt.isec.multiplayerreversi.game.interactors.networking

import android.util.Log
import pt.isec.multiplayerreversi.App.Companion.OURTAG
import pt.isec.multiplayerreversi.game.interactors.GamePlayer
import pt.isec.multiplayerreversi.game.interactors.JsonTypes
import pt.isec.multiplayerreversi.game.logic.GameData
import pt.isec.multiplayerreversi.game.logic.Player
import pt.isec.multiplayerreversi.game.logic.Profile
import java.net.Socket
import java.net.SocketException
import kotlin.concurrent.thread

class GameSetupRemoteSide(
    socket: Socket, profile: Profile,
    override val arrivedPlayerCallback: ((Player) -> Unit),
    override val leftPlayerCallback: (Player) -> Unit,
    override val hostExitedCallback: (() -> Unit),
    override val gameStartingCallback: ((GamePlayer) -> Unit),
) : AbstractNetworkingProxy(socket), IGameSetupRemoteSide {

    private var createdPlayer = false
    private var _player: Player = Player(profile)
    private val _players = ArrayList<Player>()

    init {
        try {
            beginRead()
            _players.addAll(readPlayers())
            _players.add(_player)
            endRead()

            beginSend()
            writeProfile(profile)
            endSend()

            //Player object gets its fields filled up
            beginRead()
            readPlayerIds(_player)
            endRead()

            println(_player)

            addReceiving("GameSetupRemoteSide send") {
                while (!shouldExit) {
                    try {
                        val type = beginReadAndGetType()
                        var readSomething = false
                        when (type) {
                            JsonTypes.Setup.NEW_PLAYER -> {
                                val p = Player()
                                readPlayer(p)
                                readSomething = true
                                _players.add(p)
                                arrivedPlayerCallback(p)
                            }
                            JsonTypes.Setup.LEFT_PLAYER -> {
                                val p = Player()
                                val playerId = jsonReader.nextInt()
                                readSomething = true
                                for (i in 0 until _players.size)
                                    if (_players[i].playerId == playerId) {
                                        _players.removeAt(i)
                                        break
                                    }
                                leftPlayerCallback(p)
                            }
                            JsonTypes.Setup.STARTING -> {
                                Log.i(OURTAG, "game starting")
                                val gameData = GameData()
                                gameData.players = _players
                                readStartingInformation(gameData)
                                readSomething = true

                                val gamePlayer = createGamePlayer(gameData)
                                _player.callbacks = gamePlayer
                                shouldExit = true
                                thread {
                                    // Create and thread and break this loop immediatly to avoid receiving anything in here
                                    //NOT a good way to do this, but it works
                                    gameStartingCallback(gamePlayer)
                                }
                            }
                            JsonTypes.Setup.EXITING -> {
                                hostExitedCallback()
                            }
                            else -> {
                                Log.e(OURTAG,
                                    "Received something that shouldn't have on GameSetupRemoteSide: $type")
                            }
                        }
                        Log.i(OURTAG, "Received $type GameSetupRemoteSide socket loop")
                        if (!readSomething) jsonReader.nextNull()
                        endRead()
                    } catch (e: InterruptedException) {
                        Log.i(OURTAG,"InterruptedException correu na thread GameSetupRemoteSide")
                        shouldExit = true
                        throw e
                    } catch (e: SocketException) {
                        //TODO handle errors, ask if want to continue locally or terminate
                        break
                    }
                } // while
            }

        } catch (e: SocketException) {
            Log.e(OURTAG, "Socket was close while creating LocalRemoteGameProxy")
            throw e
        }
    }

    @Synchronized
    override fun leave() {
        queueAction {
            beginSendWithType(JsonTypes.Setup.LEFT_PLAYER)
            jsonWriter.value(_player.playerId)
            endSend()
        }
    }

    override fun getPlayers(): List<Player> = _players

//    override fun close() {
//        if (!createdPlayer)
//            super.close()
//        else {
//            stopAllThreads()
//        }
//    }

    fun createGamePlayer(gameData: GameData): GamePlayerRemoteSide {
        createdPlayer = true
        this.close()
        return GamePlayerRemoteSide(socket, gameData, _player)
    }
}