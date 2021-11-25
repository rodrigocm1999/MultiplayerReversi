package pt.isec.multiplayerreversi.game.interactors.new_version

import pt.isec.multiplayerreversi.game.interactors.JsonTypes
import pt.isec.multiplayerreversi.game.logic.Game
import pt.isec.multiplayerreversi.game.logic.Player
import java.net.Socket
import kotlin.concurrent.thread

class GameSetupHostSide(
    socket: Socket, connectionsWelcomer: ConnectionsWelcomer,
    override val readyUpCallback: ((Int) -> Unit),
) : AbstractNetworkingSetupProxy(socket), IGameSetupHostSide {

    private var _player: Player

    init {
        beginSend()
        writePlayers(connectionsWelcomer.getPlayers())
        endSend()

        beginRead()
        _player = Player()
        readPlayer(_player)
        println(_player)
        //Player object gets its fields filled up
        _player.callbacks = this
        connectionsWelcomer.joinPlayer(_player, this)
        endRead()

        beginSend()
        writePlayerIds(_player.playerId, _player.piece)
        endSend()

        println(_player)

        thread {
            while (true) {
                when (beginReadAndGetType()) {
                }
                jsonReader.endObject()
            }
        }
    }

    override fun arrivedNewPlayer(player: Player) {
        beginSendWithType(JsonTypes.SetupTypes.NEW_PLAYER)
        writePlayer(player)
        endSend()
    }

    override fun sendExit() {
        beginSendWithType(JsonTypes.SetupTypes.EXITING)
        jsonWriter.nullValue()
        endSend()
    }

    override fun sendStart(game: Game) {
        beginSendWithType(JsonTypes.SetupTypes.STARTING)
        writeStartingInformation(game)
        endSend()
    }

}