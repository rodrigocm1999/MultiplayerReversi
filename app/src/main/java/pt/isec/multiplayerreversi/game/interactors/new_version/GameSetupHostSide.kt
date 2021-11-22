package pt.isec.multiplayerreversi.game.interactors.new_version

import pt.isec.multiplayerreversi.game.interactors.setup.IGameSetupHostSide
import pt.isec.multiplayerreversi.game.interactors.socket_related.ConnectionsWelcomer
import pt.isec.multiplayerreversi.game.logic.Player
import java.net.Socket
import kotlin.concurrent.thread

class GameSetupHostSide(
    socket: Socket, connectionsWelcomer: ConnectionsWelcomer,
    override val readyUpCallback: ((Int) -> Unit),
) : AbstractNetworkingSetupProxy(socket), IGameSetupHostSide {

    protected var _player: Player

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
        beginSend()
        writePlayer(player)
        endSend()
    }

}