package pt.isec.multiplayerreversi.game.interactors.new_version

import android.util.Log
import pt.isec.multiplayerreversi.App
import pt.isec.multiplayerreversi.game.interactors.GamePlayer
import pt.isec.multiplayerreversi.game.interactors.JsonTypes
import pt.isec.multiplayerreversi.game.interactors.setup.IGameSetupRemoteSide
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
                    when (beginReadAndGetType()) {
                        JsonTypes.NEW_PLAYER -> {
                            val p = Player()
                            readPlayer(p)
                            _players.add(p)
                            arrivedNewPlayerCallback(p)
                        }
                        JsonTypes.STARTING -> {
                            TODO("Need to do it yet")
                        }
                        JsonTypes.EXITING->{
                            hostExitedCallback()
                        }
                    }
                    jsonReader.endObject()
                }
            }

        } catch (e: SocketException) {
            Log.i(App.OURTAG, "Socket was close while creating LocalRemoteGameProxy")
            throw e
        }
    }

    override fun ready() {
        beginSendWithType(JsonTypes.READY)
        jsonWriter.nullValue()
        endSend()
    }

    override fun getPlayers(): List<Player> = _players
}