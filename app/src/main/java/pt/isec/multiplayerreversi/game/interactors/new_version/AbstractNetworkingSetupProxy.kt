package pt.isec.multiplayerreversi.game.interactors.new_version

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Base64
import android.util.JsonReader
import android.util.JsonToken
import android.util.JsonWriter
import androidx.core.graphics.scale
import pt.isec.multiplayerreversi.game.interactors.AbstractCallBacks
import pt.isec.multiplayerreversi.game.interactors.JsonTypes
import pt.isec.multiplayerreversi.game.logic.*
import java.io.*
import java.net.Socket

abstract class AbstractNetworkingSetupProxy(private val socket: Socket) : AbstractCallBacks(),
    Closeable {

    private val osw = OutputStreamWriter(socket.getOutputStream())
    private val osr = InputStreamReader(socket.getInputStream())
    protected lateinit var jsonWriter: JsonWriter
    protected lateinit var jsonReader: JsonReader

    protected fun readBoardArray(board: Array<Array<Piece>>) {
        val sideLength = board.size
        jsonReader.beginArray()
        for (line in 0 until sideLength) {
            jsonReader.beginArray()
            for (column in 0 until sideLength)
                board[line][column] = Piece.getByChar(jsonReader.nextString()[0])!!
            jsonReader.endArray()
        }
        jsonReader.endArray()
    }

    protected fun writeBoardArray(board: Array<Array<Piece>>) {
        val sideLength = board.size
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
                    if (jsonReader.peek() != JsonToken.NULL) {
                        val encodedImg = jsonReader.nextString()
                        var image: Drawable? = null
                        if (encodedImg != null)
                            image = decodeDrawableFromString(encodedImg)
                        player.profile.icon = image
                    } else {
                        jsonReader.nextNull()
                    }
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

    fun readStartingInformation(gameData: GameData) {
        jsonReader.beginObject()
        while (jsonReader.hasNext()) {
            when (jsonReader.nextName()) {
                "sideLength" -> {
                    val side = jsonReader.nextInt()
                    gameData.sideLength = side
                    gameData.board = Array(side) { Array(side) { Piece.Empty } }
                }
                "board" -> {
                    readBoardArray(gameData.board)
                }
                "startingPlayerId" ->
                    gameData.currentPlayer =
                        gameData.players.find { it.playerId == jsonReader.nextInt() }!!
            }
        }
        jsonReader.endObject()
    }

    fun writeStartingInformation(game: Game) {
        jsonWriter.beginObject()
        jsonWriter.name("sideLength").value(game.sideLength)
        jsonWriter.name("board")
        writeBoardArray(game.board)
        jsonWriter.name("startingPlayerId").value(game.currentPlayer.playerId)
        jsonWriter.endObject()
    }

    protected fun beginSend() {
        jsonWriter = JsonWriter(osw)
        jsonWriter.beginObject()
        jsonWriter.name(JsonTypes.SetupTypes.DATA)
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

    protected fun writeType(type: String) {
        jsonWriter.name("type").value(type)
    }

    protected fun beginReadAndGetType(): String {
        jsonReader = JsonReader(osr)
        jsonReader.beginObject()
        jsonReader.nextName() // "type" :
        val type = jsonReader.nextString()
        jsonReader.nextName() // "data" :
        return type
    }

    protected fun beginSendWithType(type: String) {
        jsonWriter = JsonWriter(osw)
        jsonWriter.beginObject()
        writeType(type)
        jsonWriter.name(JsonTypes.SetupTypes.DATA)
    }

    private fun encodeDrawableToString(drawable: Drawable): String {
        return Base64.encodeToString(convertDrawableToByteArray(drawable), Base64.DEFAULT)
    }

    private fun decodeDrawableFromString(encodedImg: String): Drawable {
        return convertByteArrayToDrawable(Base64.decode(encodedImg, Base64.DEFAULT))
    }

    private fun convertDrawableToByteArray(drawable: Drawable): ByteArray {
        var bitmap = (drawable as BitmapDrawable).bitmap
        bitmap = bitmap.scale(200, 300)
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 94, stream)
        return stream.toByteArray()
    }

    private fun convertByteArrayToDrawable(byteArray: ByteArray): Drawable {
        val stream = ByteArrayInputStream(byteArray)
        return BitmapDrawable.createFromStream(stream, "src")
    }

    override fun close() {
        socket.close()
    }
}