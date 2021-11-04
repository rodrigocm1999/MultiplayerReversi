package pt.isec.multiplayerreversi.gamelogic

class Player {
    private val profile: Profile? = null
    private val piece: Piece? = null
    private val hasUsedBomb = false
    private val hasUsedTrade = false

    fun getProfile(): Profile? = profile
    fun getPiece(): Piece? = piece
    fun hasUsedBomb(): Boolean = hasUsedBomb
    fun hasUsedTrade(): Boolean = hasUsedTrade
}