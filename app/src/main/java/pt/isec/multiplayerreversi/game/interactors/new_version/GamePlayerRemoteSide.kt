package pt.isec.multiplayerreversi.game.interactors.new_version

import android.util.Log
import pt.isec.multiplayerreversi.App.Companion.OURTAG
import pt.isec.multiplayerreversi.game.interactors.GamePlayer
import pt.isec.multiplayerreversi.game.interactors.JsonTypes
import pt.isec.multiplayerreversi.game.logic.GameData
import pt.isec.multiplayerreversi.game.logic.Player
import pt.isec.multiplayerreversi.game.logic.Vector
import java.net.Socket
import java.util.*
import kotlin.concurrent.thread

class GamePlayerRemoteSide(
    socket: Socket,
    private val gameData: GameData,
    private val ownPlayer: Player,
) :
    AbstractNetworkingSetupProxy(socket), GamePlayer {

    init {
        thread {
            while (true) {
                val type = beginReadAndGetType()
                var readSomething = false
                when (type) {
//                    JsonTypes.InGameTypes.GAME_STARTED -> {}
                    JsonTypes.InGameTypes.BOARD_CHANGED -> {
                        readBoardArray(gameData.board)
                        readSomething = true
                        Log.i(OURTAG, "Read Board ${gameData.board}")

                        updateBoardCallback?.invoke(gameData.board)
                    }
                    //TODO 4 these things
                    JsonTypes.InGameTypes.NORMAL_PLAY -> {}
                    JsonTypes.InGameTypes.BOMB_PLAY -> {}
                    JsonTypes.InGameTypes.TRADE_PLAY -> {}
                    JsonTypes.InGameTypes.GAME_FINISHED -> {}
                    else -> {}

                }
                if (!readSomething) jsonReader.nextNull()
                jsonReader.endObject()
            }
        }
    }

    override fun playAt(line: Int, column: Int) {
        TODO("Not yet implemented")
    }

    override fun playBomb(line: Int, column: Int) {
        TODO("Not yet implemented")
    }

    override fun playTrade(tradePieces: ArrayList<Vector>) {
        TODO("Not yet implemented")
    }

    override fun isOnline() = true
    override fun getPlayers() = gameData.players
    override fun getOwnPlayer() = ownPlayer
    override fun getGameBoard() = gameData.board
}