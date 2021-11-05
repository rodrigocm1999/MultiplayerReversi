package pt.isec.multiplayerreversi.gamelogic

class Player(piece: Piece) {

    private val profile: Profile? = null
    private val piece: Piece = piece
    private val hasUsedBomb = false
    private val hasUsedTrade = false

    fun getProfile(): Profile? = profile
    fun getPiece(): Piece = piece
    fun hasUsedBomb(): Boolean = hasUsedBomb
    fun hasUsedTrade(): Boolean = hasUsedTrade
}