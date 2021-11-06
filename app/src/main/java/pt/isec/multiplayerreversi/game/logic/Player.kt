package pt.isec.multiplayerreversi.game.logic

class Player(
    private val piece: Piece,
    private val profile: Profile
) {

    constructor(piece: Piece, profile: Profile, hasUsedBomb: Boolean, hasUsedTrade: Boolean)
            : this(piece, profile) {
        this.hasUsedBomb = hasUsedBomb
        this.hasUsedTrade = hasUsedTrade
    }

    private var hasUsedBomb = false
    private var hasUsedTrade = false

    fun getProfile() = profile
    fun getPiece() = piece
    fun hasUsedBomb() = hasUsedBomb
    fun hasUsedTrade() = hasUsedTrade
}