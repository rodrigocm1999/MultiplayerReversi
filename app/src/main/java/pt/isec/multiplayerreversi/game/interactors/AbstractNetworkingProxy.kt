package pt.isec.multiplayerreversi.game.interactors

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Base64
import android.util.JsonReader
import android.util.JsonWriter
import pt.isec.multiplayerreversi.game.logic.Piece
import pt.isec.multiplayerreversi.game.logic.Player
import pt.isec.multiplayerreversi.game.logic.Profile
import java.io.*
import java.net.Socket

abstract class AbstractNetworkingProxy(private val socket: Socket) : CallBacksProxy(),
    GamePlayer, Closeable {

    private val osw = OutputStreamWriter(socket.getOutputStream())
    private val osr = InputStreamReader(socket.getInputStream())
    protected lateinit var jsonWriter: JsonWriter // JsonWriter(osw)
    protected lateinit var jsonReader: JsonReader  // JsonReader(osr)

    protected lateinit var _player: Player
    protected val _players = ArrayList<Player>(3)
    protected var _gameSideLength = -1


    protected fun readBoardArray(): Array<Array<Piece>> {
        val sideLength = getGameSideLength()
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

    protected fun sendBoardArray(board: Array<Array<Piece>>) {
        jsonWriter.beginObject()
        writeType(JsonTypes.BOARD)
        writeBoardArray(board)
        jsonWriter.endObject()
        jsonWriter.flush()
    }

    protected fun writeBoardArray(board: Array<Array<Piece>>) {
        val sideLength = getGameSideLength()
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

    private fun writeLineColumn(line: Int, column: Int) {
        jsonWriter.name("x").value(column)
        jsonWriter.name("y").value(line)
    }

    private fun writeType(type: String) {
        jsonWriter.name("type").value(type)
    }

    protected fun readType(): String {
        return jsonReader.nextString()
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

    fun writePlayer(it: Player) {
        jsonWriter.beginObject()
        jsonWriter.name("name").value(it.profile.name)
        jsonWriter.name("id").value(it.playerId)
        jsonWriter.name("piece").value(it.piece.char.toString())
        val icon = it.profile.icon
        if (icon != null)
            jsonWriter.name("image").value(encodeDrawableToString(icon))
        else
            jsonWriter.name("image").nullValue()
        jsonWriter.endObject()
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

    protected fun readPlayer(player: Player) {
        jsonReader.beginObject()
        while (jsonReader.hasNext()) {
            when (jsonReader.nextName()) {
                "id" -> player.playerId = jsonReader.nextInt()
                "piece" -> player.piece = Piece.getByChar(jsonReader.nextString()[0])!!
                "name" -> player.profile.name = jsonReader.nextString()
                "image" -> {
                    val encodedImg = jsonReader.nextString()
                    var image: Drawable? = null
                    if (encodedImg != null)
                        image = decodeDrawableFromString(encodedImg)
                    player.profile.icon = image
                }
            }
        }
        jsonReader.endObject()
    }

    protected fun writeProfile(profile: Profile) {
        jsonWriter.beginObject()
        jsonWriter.name("name").value(profile.name)
        val icon = profile.icon
        if (icon != null)
            jsonWriter.name("image").value(encodeDrawableToString(icon))
        else
            jsonWriter.name("image").nullValue()
        jsonWriter.endObject()
    }

    protected fun sendProfile(profile: Profile) {
        jsonWriter.beginObject()
        writeType(JsonTypes.SEND_PROFILE)
        jsonWriter.name(JsonTypes.DATA)
        writeProfile(profile)
        jsonWriter.endObject()
        jsonWriter.flush()
    }

    protected fun readProfile(profile: Profile) {
        jsonReader.beginObject()
        when (jsonReader.nextName()) {
            "name" -> profile.name = jsonReader.nextString()
            "image" -> {
                val encodedImg = jsonReader.nextString()
                var image: Drawable? = null
                if (encodedImg != null)
                    image = decodeDrawableFromString(encodedImg)
                profile.icon = image
            }
        }
        jsonReader.endObject()
    }

    protected fun beginSend() {
        jsonWriter = JsonWriter(osw)
        jsonWriter.beginObject()
        jsonWriter.name(JsonTypes.DATA)
    }

    protected fun endSend() {
        jsonWriter.endObject()
        jsonWriter.flush()
    }

    protected fun beginRead() {
        jsonReader = JsonReader(osr)
        jsonReader.beginObject()
        jsonReader.nextName()
    }

    protected fun endRead() {
        jsonReader.endObject()
    }


    protected fun sendPlayerIds(id: Int, piece: Piece) {
        jsonWriter.beginObject()
        writeType(JsonTypes.PLAYER_IDS)
        jsonWriter.name(JsonTypes.DATA)
        writePlayerIds(id, piece)
        jsonWriter.endObject()
        jsonWriter.flush()
    }

    protected fun writePlayerIds(id: Int, piece: Piece) {
        jsonWriter.beginObject()
        jsonWriter.name("id").value(id)
        jsonWriter.name("piece").value(piece.char.toString())
        jsonWriter.endObject()
    }

    protected fun readPlayerIds(player: Player) {
        jsonReader.beginObject()
        while (jsonReader.hasNext()) {
            when (jsonReader.nextName()) {
                "id" -> player.playerId = jsonReader.nextInt()
                "piece" -> player.piece = Piece.getByChar(jsonReader.nextString()[0])!!
            }
        }
        jsonReader.endObject()
    }

    private fun encodeDrawableToString(drawable: Drawable): String {
        return Base64.encodeToString(convertDrawableToByteArray(drawable), Base64.DEFAULT)
    }

    private fun decodeDrawableFromString(encodedImg: String): Drawable {
        return convertByteArrayToDrawable(Base64.decode(encodedImg, Base64.DEFAULT))
    }

    private fun convertDrawableToByteArray(drawable: Drawable): ByteArray {
        val bitmap = (drawable as BitmapDrawable).bitmap
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 94, stream)
        return stream.toByteArray()
    }

    private fun convertByteArrayToDrawable(byteArray: ByteArray): Drawable {
        val stream = ByteArrayInputStream(byteArray)
        return BitmapDrawable.createFromStream(stream, "src")
    }

    override fun close() {
        jsonReader.close()
        jsonWriter.close()
        socket.close()
    }

    override fun getPlayers() = _players
    override fun getOwnPlayer() = _player
    override fun getGameSideLength() = _gameSideLength

}