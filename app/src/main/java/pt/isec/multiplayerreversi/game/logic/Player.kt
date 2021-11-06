package pt.isec.multiplayerreversi.game.gamelogic

class Player(
    private val piece: Piece,
    private val profile: Profile
) {

    private var hasUsedBomb = false
    private var hasUsedTrade = false

    fun getProfile() = profile
    fun getPiece() = piece
    fun hasUsedBomb() = hasUsedBomb
    fun hasUsedTrade() = hasUsedTrade
}