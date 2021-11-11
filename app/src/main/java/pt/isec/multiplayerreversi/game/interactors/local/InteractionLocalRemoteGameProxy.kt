package pt.isec.multiplayerreversi.game.interactors.local

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Base64
import android.util.JsonReader
import android.util.JsonWriter
import pt.isec.multiplayerreversi.game.interactors.JsonTypes
import pt.isec.multiplayerreversi.game.logic.Piece
import pt.isec.multiplayerreversi.game.logic.Player
import pt.isec.multiplayerreversi.game.logic.Profile
import java.io.ByteArrayOutputStream
import java.io.Closeable
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.Socket

class InteractionLocalRemoteGameProxy(private val socket: Socket, private val profile: Profile) :
    AbstractCallbacksProxy(), Closeable {

    private val jsonWriter: JsonWriter = JsonWriter(OutputStreamWriter(socket.getOutputStream()))
    private val jsonReader: JsonReader = JsonReader(InputStreamReader(socket.getInputStream()))

    private lateinit var player: Player
    private lateinit var players: List<Player>
    private var gameSideLength: Int = -1


    init {
        //TODO 10 do the initial handshake, receive profile


        jsonWriter.beginObject()
        sendType(JsonTypes.ASK_CAN_ENTER)
        jsonWriter.endObject()


    }

    fun readBoardArray(): Array<Array<Piece>> {
        val sideLength = getGameSideLength()
        val board = Array(sideLength) { Array(sideLength) { Piece.Empty } }

        jsonReader.beginArray()
        for (line in 0 until sideLength) {
            jsonReader.beginArray()
            for (column in 0 until sideLength) {
                board[line][column] = Piece.getById(jsonReader.nextInt())!!
            }
            jsonReader.endArray()
        }
        jsonReader.endArray()

        return board
    }

    private fun sendLineColumn(line: Int, column: Int) {
        jsonWriter.name("x").value(column)
        jsonWriter.name("y").value(line)
    }

    private fun sendType(type: Int) {
        jsonWriter.name("type").value(type)
    }

    private fun encodeImageToString(byteArray: ByteArray): String {
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    private fun decodeImageFromString(encodedImg: String): ByteArray {
        return Base64.decode(encodedImg, Base64.DEFAULT)
    }

    private fun convertDrawableToByteArray(drawable: Drawable): ByteArray {
        val bitmap = (drawable as BitmapDrawable).bitmap
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 94, stream)
        return stream.toByteArray()
    }

    override fun close() {
        jsonReader.close()
        jsonWriter.close()
        socket.close()
    }

    override fun playAt(line: Int, column: Int) {
        TODO("Not yet implemented")
    }

    override fun playBomb(line: Int, column: Int) {
        TODO("Not yet implemented")
    }

    override fun getPlayers() = players

    override fun getOwnPlayer() = player
    override fun getGameSideLength() = gameSideLength
}