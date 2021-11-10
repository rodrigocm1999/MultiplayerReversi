package pt.isec.multiplayerreversi.game.logic

import java.io.Serializable

class Player(
    private val profile: Profile,
    private val piece: Piece = Piece.Empty,
) : Serializable {

    constructor(profile: Profile, piece: Piece, hasUsedBomb: Boolean, hasUsedTrade: Boolean)
            : this(profile, piece) {
        this.hasUsedBomb = hasUsedBomb
        this.hasUsedTrade = hasUsedTrade
    }

    companion object {
        private var playerIdCounter = 0
    }

    private var playerId = playerIdCounter++

    var hasUsedBomb = false
    var hasUsedTrade = false

    fun getPlayerId() = playerId
    fun getProfile() = profile
    fun getPiece() = piece
    fun hasUsedBomb() = hasUsedBomb
    fun hasUsedTrade() = hasUsedTrade
}