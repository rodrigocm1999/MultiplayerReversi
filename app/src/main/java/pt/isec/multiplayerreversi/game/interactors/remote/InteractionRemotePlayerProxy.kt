package pt.isec.multiplayerreversi.game.interactors.remote

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Base64
import android.util.JsonReader
import android.util.JsonWriter
import pt.isec.multiplayerreversi.game.logic.Profile
import java.io.ByteArrayOutputStream
import java.io.Closeable
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.Socket


class InteractionRemotePlayerProxy(private val socket: Socket, private val player: Profile) : Closeable {

    private val jsonWriter: JsonWriter = JsonWriter(OutputStreamWriter(socket.getOutputStream()))
    private val jsonReader: JsonReader = JsonReader(InputStreamReader(socket.getInputStream()))

    init {
        //TODO 10 do the initial handshake, send player name and profile
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
}