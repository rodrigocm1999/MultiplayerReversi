package pt.isec.multiplayerreversi.game.remote

import android.util.JsonReader
import android.util.JsonWriter
import pt.isec.multiplayerreversi.game.logic.Piece
import pt.isec.multiplayerreversi.game.logic.Player
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.Socket

class RemoteGameSetup(socket: Socket) : AbstractRemoteSetup(socket) {

    protected lateinit var _player: Player
    protected lateinit var _players: ArrayList<Player>
    protected var _gameSideLength = -1

    protected fun readBoardArray(): Array<Array<Piece>> {
        val sideLength = _gameSideLength
        val board = Array(sideLength) { Array(sideLength) { Piece.Empty } }
        jsonReader.beginArray()
        for (line in 0 until sideLength) {
            jsonReader.beginArray()
            for (column in 0 until sideLength)
                board[line][column] = Piece.getByChar(jsonReader.nextString()[0])!!
            jsonReader.endArray()
        }
        jsonReader.endArray()
        return board
    }
}