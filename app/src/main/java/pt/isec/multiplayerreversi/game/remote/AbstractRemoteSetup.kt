package pt.isec.multiplayerreversi.game.remote

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
import java.io.*
import java.net.Socket

abstract class AbstractRemoteSetup(private val socket: Socket) : Closeable, AutoCloseable {

    protected val jsonWriter: JsonWriter = JsonWriter(OutputStreamWriter(socket.getOutputStream()))
    protected val jsonReader: JsonReader = JsonReader(InputStreamReader(socket.getInputStream()))

    protected fun writeLineColumn(line: Int, column: Int) {
        jsonWriter.name("x").value(column)
        jsonWriter.name("y").value(line)
    }

    protected fun writeType(type: String) {
        jsonWriter.name("type").value(type)
    }

    protected fun readType(): String {
        return jsonReader.nextString()
    }


    protected fun writePlayer(it: Player) {
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

    protected fun readPlayer(player: Player) {
        jsonReader.beginObject()
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


    protected fun writePlayerIds(id: Int, piece: Piece) {
        jsonWriter.beginObject()
        jsonWriter.name("id").value(id)
        jsonWriter.name("piece").value(piece.char.toString())
        jsonWriter.endObject()
    }

    protected fun readPlayerIds(player: Player) {
        do {
            when (jsonReader.nextName()) {
                "id" -> player.playerId = jsonReader.nextInt()
                "piece" -> player.piece = Piece.getByChar(jsonReader.nextString()[0])!!
            }
        } while (jsonReader.hasNext())
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

}