package pt.isec.multiplayerreversi.game.interactors.new_version

import android.util.Log
import pt.isec.multiplayerreversi.App.Companion.OURTAG
import pt.isec.multiplayerreversi.game.interactors.GameCallbacks
import pt.isec.multiplayerreversi.game.interactors.JsonTypes
import pt.isec.multiplayerreversi.game.logic.Game
import pt.isec.multiplayerreversi.game.logic.Player
import java.lang.Exception
import java.net.Socket
import kotlin.concurrent.thread

class GameSetupHostSide(
    socket: Socket, connectionsWelcomer: ConnectionsWelcomer,
    override val readyUpCallback: ((Int) -> Unit),
) : AbstractNetworkingSetupProxy(socket), IGameSetupHostSide {

    private var _player: Player

    private var shouldExit = false

    init {
        beginSend()
        writePlayers(connectionsWelcomer.getPlayers())
        endSend()

        beginRead()
        _player = Player()
        readPlayer(_player)
        connectionsWelcomer.joinPlayer(_player, this)
        endRead()

        beginSend()
        writePlayerIds(_player.playerId, _player.piece)
        endSend()

        thread {
            try {
                while (!shouldExit) {
                    val type = beginReadAndGetType()
                    var readSomething = false
                    when (type) {
                        JsonTypes.Setup.LEFT_PLAYER -> {
                            connectionsWelcomer.playerLeft(_player)
                            shouldExit = true
                        }
                    }
                    if (!readSomething) jsonReader.nextNull()
                    jsonReader.endObject()
                }
            } catch (e: Exception) {
                connectionsWelcomer.playerLeft(_player)
                Log.e(OURTAG, "", e)
            }
        }
    }

    override fun arrivedPlayer(player: Player) {
        beginSendWithType(JsonTypes.Setup.NEW_PLAYER)
        writePlayer(player)
        endSend()
    }

    override fun leftPayer(playerId: Int) {
        beginSendWithType(JsonTypes.Setup.LEFT_PLAYER)
        jsonWriter.value(playerId)
        endSend()
    }

    override fun sendExit() {
        beginSendWithType(JsonTypes.Setup.EXITING)
        jsonWriter.nullValue()
        endSend()
    }

    override fun sendStart(game: Game) {
        beginSendWithType(JsonTypes.Setup.STARTING)
        writeStartingInformation(game)
        endSend()
    }

    override fun createGamePlayer(game: Game): GameCallbacks {
        return GamePlayerHostSide(game, _player, socket)
    }
}