package pt.isec.multiplayerreversi.game.remote

import android.util.JsonReader
import android.util.JsonWriter
import pt.isec.multiplayerreversi.game.interactors.JsonTypes
import pt.isec.multiplayerreversi.game.logic.Piece
import pt.isec.multiplayerreversi.game.logic.Player
import pt.isec.multiplayerreversi.game.logic.Profile
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.Socket

class RemotePlayerSetup(socket: Socket) : AbstractRemoteSetup(socket) {

    protected fun sendBoardArray(board: Array<Array<Piece>>) {
        jsonWriter.beginObject()
        writeType(JsonTypes.BOARD)
        writeBoardArray(board)
        jsonWriter.endObject()
        jsonWriter.flush()
    }

    protected fun writeBoardArray(board: Array<Array<Piece>>) {
        val sideLength = board.size
        jsonWriter.name("board")
        jsonWriter.beginArray()
        for (line in 0 until sideLength) {
            jsonWriter.beginArray()
            for (column in 0 until sideLength)
                jsonWriter.value(board[line][column].char.toString())
            jsonWriter.endArray()
        }
        jsonWriter.endArray()
    }

    protected fun sendPlayers(players: List<Player>) {
        jsonWriter.beginObject()
        writeType(JsonTypes.PLAYERS)
        jsonWriter.name(JsonTypes.DATA)
        writePlayers(players)
        jsonWriter.endObject()
        jsonWriter.flush()
    }

    protected fun writePlayers(players: List<Player>) {
        jsonWriter.beginArray()
        players.forEach {
            writePlayer(it)
        }
        jsonWriter.endArray()
    }

    protected fun readPlayers(): ArrayList<Player> {
        val players = ArrayList<Player>(3)
        jsonReader.beginArray()
        while (jsonReader.hasNext()) {
            val player = Player(Profile())
            readPlayer(player)
            players.add(player)
        }
        jsonReader.endArray()
        return players
    }

    protected fun sendPlayerIds(id: Int, piece: Piece) {
        jsonWriter.beginObject()
        writeType(JsonTypes.PLAYER_IDS)
        jsonWriter.name(JsonTypes.DATA)
        writePlayerIds(id, piece)
        jsonWriter.endObject()
        jsonWriter.flush()
    }

}