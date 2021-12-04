package pt.isec.multiplayerreversi.game.interactors.networking

import android.util.Log
import pt.isec.multiplayerreversi.App.Companion.OURTAG
import pt.isec.multiplayerreversi.game.interactors.GameCallbacks
import pt.isec.multiplayerreversi.game.interactors.JsonTypes
import pt.isec.multiplayerreversi.game.logic.Game
import pt.isec.multiplayerreversi.game.logic.Player
import java.lang.Exception
import java.net.Socket

class GameSetupHostSide(
    socket: Socket, connectionsWelcomer: ConnectionsWelcomer,
    override val readyUpCallback: ((Int) -> Unit),
) : AbstractNetworkingProxy(socket), IGameSetupHostSide {

    private var _player: Player
    private var createdPlayer = false

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

        addReceiving("GameSetupHostSide receive") {
            try {
                while (!shouldExit) {
                    Log.i(OURTAG,"STUCK rn pls help")
                    val type = beginReadAndGetType()
                    Log.i(OURTAG,"STUCK rn pls help -> andou para a frente")
                    var readSomething = false
                    when (type) {
                        JsonTypes.Setup.LEFT_PLAYER -> {
                            val pId = jsonReader.nextInt()
                            readSomething = true
                            connectionsWelcomer.playerLeft(_player)
                            shouldExit = true
                        }
                        else->{
                            Log.e(OURTAG,
                                "Received something that shouldn't have on GameSetupHostSide: $type")
                        }
                    }
                    if (!readSomething) jsonReader.nextNull()
                    endRead()
                }
            } catch (e: InterruptedException) {
                Log.i(OURTAG,"InterruptedException correu na thread GameSetupHostSide")
                shouldExit = true
                throw e
            } catch (e: Exception) {
                connectionsWelcomer.playerLeft(_player)
                Log.e(OURTAG, "", e)
            }
        }
    }

    override fun arrivedPlayer(player: Player) {
        queueAction {
            beginSendWithType(JsonTypes.Setup.NEW_PLAYER)
            writePlayer(player)
            endSend()
        }
    }

    override fun leftPayer(playerId: Int) {
        queueAction {
            beginSendWithType(JsonTypes.Setup.LEFT_PLAYER)
            jsonWriter.value(playerId)
            endSend()
        }
    }

    override fun sendExit() {
        queueAction {
            beginSendWithType(JsonTypes.Setup.EXITING)
            jsonWriter.nullValue()
            endSend()
        }
    }

    override fun sendStart(game: Game) {
        queueAction {
            shouldExit = true
            beginSendWithType(JsonTypes.Setup.STARTING)
            writeStartingInformation(game)
            endSend()
            Log.i(OURTAG,"Send STARTING correu")
        }
    }

    override fun createGamePlayer(game: Game): GameCallbacks {
        createdPlayer = true
        close()
        Log.i(OURTAG,"mandou fechar setuper")
        return GamePlayerHostSide(game, _player, socket)
    }
}