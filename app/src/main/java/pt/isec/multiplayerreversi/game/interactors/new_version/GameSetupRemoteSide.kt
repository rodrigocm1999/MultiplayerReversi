package pt.isec.multiplayerreversi.game.interactors.new_version

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
    override val arrivedNewPlayerCallback: ((Player) -> Unit),
    override val hostExitedCallback: (() -> Unit),
    override val gameStartingCallback: ((GamePlayer) -> Unit),
) : AbstractNetworkingSetupProxy(socket), IGameSetupRemoteSide {

    protected var _player: Player = Player(profile, callbacks = this)
    protected val _players = ArrayList<Player>()

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

            thread {
                while (true) {
                    val type = beginReadAndGetType()
                    var readSomething = false
                    when (type) {
                        JsonTypes.SetupTypes.NEW_PLAYER -> {
                            val p = Player()
                            readPlayer(p)
                            readSomething = true
                            _players.add(p)
                            arrivedNewPlayerCallback(p)
                        }
                        JsonTypes.SetupTypes.STARTING -> {
                            Log.i(OURTAG, "game starting")
                            val gameData = GameData()
                            gameData.players = _players
                            readStartingInformation(gameData)
                            readSomething = true

                            val gamePlayer = GamePlayerRemoteSide(socket, gameData, _player)
                            gameStartingCallback(gamePlayer)
                        }
                        JsonTypes.SetupTypes.EXITING -> {
                            hostExitedCallback()
                        }
                        else -> {
                            Log.i(OURTAG,
                                "Recieved something that shouldn't have on GameSetupRemoteSide: $type")
                        }
                    }
                    if (!readSomething) jsonReader.nextNull()
                    jsonReader.endObject()
                }
            }

        } catch (e: SocketException) {
            Log.i(OURTAG, "Socket was close while creating LocalRemoteGameProxy")
            throw e
        }
    }

    override fun ready() {
        beginSendWithType(JsonTypes.SetupTypes.READY)
        jsonWriter.nullValue()
        endSend()
    }

    override fun getPlayers(): List<Player> = _players
}