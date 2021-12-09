package pt.isec.multiplayerreversi.game.interactors.networking

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.util.Base64
import android.util.JsonReader
import android.util.JsonToken
import android.util.JsonWriter
import androidx.core.graphics.scale
import pt.isec.multiplayerreversi.game.logic.*
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

fun JsonReader.readBoardArray(board: Array<Array<Piece>>) {
    val sideLength = board.size
    this.beginArray()
    for (line in 0 until sideLength) {
        this.beginArray()
        for (column in 0 until sideLength)
            board[line][column] = Piece.getByChar(this.nextString()[0])!!
        this.endArray()
    }
    this.endArray()
}
fun JsonReader.readScoresArray(players: List<Player>){
    this.beginArray()
    players.forEach {
        it.score = this.nextInt()
    }
    this.endArray()
}

fun JsonWriter.writeBoardArray(board: Array<Array<Piece>>) {
    val sideLength = board.size
    this.beginArray()
    for (line in 0 until sideLength) {
        this.beginArray()
        for (column in 0 until sideLength)
            this.value(board[line][column].char.toString())
        this.endArray()
    }
    this.endArray()
}
fun JsonWriter.writeScoresArray(players: List<Player>){
    this.beginArray()
    players.forEach {
        this.value(it.score)
    }
    this.endArray()
}

fun JsonWriter.writeVector(vector: Vector) {
    this.beginArray()
    this.value(vector.x).value(vector.y)
    this.endArray()
}


fun JsonReader.readVector(): Vector {
    this.beginArray()
    val v = Vector(this.nextInt(), this.nextInt())
    this.endArray()
    return v
}

fun JsonWriter.writePlayers(players: List<Player>) {
    this.beginArray()
    players.forEach {
        writePlayer(it)
    }
    this.endArray()
}

fun JsonWriter.writePlayer(it: Player) {
    this.beginObject()
    this.name("name").value(it.profile.name)
    this.name("id").value(it.playerId)
    this.name("piece").value(it.piece.char.toString())
    val icon = it.profile.icon
    if (icon != null)
        this.name("image").value(encodeDrawableToString(icon))
    else
        this.name("image").nullValue()
    this.endObject()
}

fun JsonReader.readPlayers(): ArrayList<Player> {
    val players = ArrayList<Player>(3)
    this.beginArray()
    while (this.hasNext()) {
        val player = Player(Profile())
        this.readPlayer(player)
        players.add(player)
    }
    this.endArray()
    return players
}

fun JsonReader.readPlayer(player: Player) {
    this.beginObject()
    while (this.hasNext()) {
        when (this.nextName()) {
            "id" -> player.playerId = this.nextInt()
            "piece" -> player.piece = Piece.getByChar(this.nextString()[0])!!
            "name" -> player.profile.name = this.nextString()
            "image" -> {
                if (this.peek() != JsonToken.NULL) {
                    val encodedImg = this.nextString()
                    var image: BitmapDrawable? = null
                    if (encodedImg != null)
                        image = decodeDrawableFromString(encodedImg)
                    player.profile.icon = image
                } else {
                    this.nextNull()
                }
            }
        }
    }
    this.endObject()
}

fun JsonWriter.writeProfile(profile: Profile) {
    this.beginObject()
    this.name("name").value(profile.name)
    val icon = profile.icon
    if (icon != null)
        this.name("image").value(encodeDrawableToString(icon))
    else
        this.name("image").nullValue()
    this.endObject()
}

fun JsonWriter.writePlayerIds(id: Int, piece: Piece) {
    this.beginObject()
    this.name("id").value(id)
    this.name("piece").value(piece.char.toString())
    this.endObject()
}

fun JsonReader.readPlayerIds(player: Player) {
    this.beginObject()
    while (this.hasNext()) {
        when (this.nextName()) {
            "id" -> player.playerId = this.nextInt()
            "piece" -> player.piece = Piece.getByChar(this.nextString()[0])!!
        }
    }
    this.endObject()
}

fun JsonReader.readStartingInformation(gameData: GameData) {
    this.beginObject()
    while (this.hasNext()) {
        when (this.nextName()) {
            "sideLength" -> {
                val side = this.nextInt()
                gameData.sideLength = side
                gameData.board = Array(side) { Array(side) { Piece.Empty } }
            }
            "board" -> {
                readBoardArray(gameData.board)
            }
            "startingPlayerId" -> {
                val id = this.nextInt()
                gameData.currentPlayer = gameData.players.find { it.playerId == id }!!
            }
        }
    }
    this.endObject()
}

fun JsonWriter.writeStartingInformation(game: Game) {
    this.beginObject()
    this.name("sideLength").value(game.sideLength)
    this.name("board")
    writeBoardArray(game.board)
    this.name("startingPlayerId").value(game.currentPlayer.playerId)
    this.endObject()
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