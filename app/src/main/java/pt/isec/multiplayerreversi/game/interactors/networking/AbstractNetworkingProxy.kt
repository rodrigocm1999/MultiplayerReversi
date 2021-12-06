package pt.isec.multiplayerreversi.game.interactors.networking

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.util.*
import androidx.core.graphics.scale
import org.json.JSONObject
import pt.isec.multiplayerreversi.App.Companion.OURTAG
import pt.isec.multiplayerreversi.game.interactors.JsonTypes
import pt.isec.multiplayerreversi.game.logic.*
import java.io.*
import java.lang.Exception
import java.net.Socket
import java.util.concurrent.ArrayBlockingQueue
import kotlin.concurrent.thread

abstract class AbstractNetworkingProxy(protected val socket: Socket) : Closeable {

    private val osw = OutputStreamWriter((socket.getOutputStream()))
    private val osr = InputStreamReader((socket.getInputStream()))
    protected var jsonWriter: JsonWriter = JsonWriter(osw)
    protected var jsonReader: JsonReader = JsonReader(osr)

    private val queuedActions = ArrayBlockingQueue<() -> Unit>(10)
    private lateinit var receivingThread: Thread
    private val senderThread: Thread

    @Volatile
    protected var shouldExit = false

    init {
        jsonWriter.beginArray().flush()
        jsonReader.beginArray()

        senderThread = thread {
            while (!shouldExit) {
                val block = queuedActions.take()
                block()
            }
        }
    }

    protected fun queueAction(block: () -> Unit) {
        queuedActions.put(block)
    }

    private fun stopAllThreads() {
        senderThread.interrupt()
        receivingThread.interrupt()
    }

    protected fun setReceiving(threadName: String, runner: () -> Unit) {
        receivingThread = thread {
            try {
                Log.i(OURTAG, "Started Thread with name : $threadName")
                runner()
                Log.i(OURTAG, "Finished Thread with name : $threadName")
            } catch (e: InterruptedException) {
                Log.i(OURTAG, "Interrupted Thread with name : $threadName")
            }
        }
    }

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

    protected fun writeVector(vector: Vector) {
        jsonWriter.beginArray()
        jsonWriter.value(vector.x).value(vector.y)
        jsonWriter.endArray()
    }


    protected fun readVector(): Vector {
        jsonReader.beginArray()
        val v = Vector(jsonReader.nextInt(), jsonReader.nextInt())
        jsonReader.endArray()
        return v
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
                        var image: BitmapDrawable? = null
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
                "startingPlayerId" -> {
                    val id = jsonReader.nextInt()
                    gameData.currentPlayer = gameData.players.find { it.playerId == id }!!
                }
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

    private fun writeType(type: String) {
        jsonWriter.name("type").value(type)
    }

    protected fun beginSendWithType(type: String) {
        jsonWriter.beginObject()
        writeType(type)
        jsonWriter.name(JsonTypes.Setup.DATA)
    }

    protected fun beginReadAndGetType(): String {
        jsonReader.beginObject()
        jsonReader.nextName() // "type" :
        val type = jsonReader.nextString()
        jsonReader.nextName() // "data" :
        return type
    }

    protected fun endSend() {
        jsonWriter.endObject()
        jsonWriter.flush()
    }

    protected fun endRead() {
        jsonReader.endObject()
    }

    private fun encodeDrawableToString(drawable: BitmapDrawable): String {
        return Base64.encodeToString(convertDrawableToByteArray(drawable), Base64.DEFAULT)
    }

    private fun decodeDrawableFromString(encodedImg: String): BitmapDrawable {
        return convertByteArrayToDrawable(Base64.decode(encodedImg, Base64.DEFAULT))
    }

    private fun convertDrawableToByteArray(drawable: BitmapDrawable): ByteArray {
        var bitmap = drawable.bitmap
        bitmap = bitmap.scale(200, 300)
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 94, stream)
        return stream.toByteArray()
    }

    private fun convertByteArrayToDrawable(byteArray: ByteArray): BitmapDrawable {
        val stream = ByteArrayInputStream(byteArray)
        return BitmapDrawable.createFromStream(stream, "src") as BitmapDrawable
    }

    override fun close() {
        synchronized(queuedActions) {
            shouldExit = true
            if (!queuedActions.isEmpty()) {
                senderThread.join()
            }
        }
        stopAllThreads()
        //TODO  quando se sai do jogo tem de fechar os sockets
    }
}