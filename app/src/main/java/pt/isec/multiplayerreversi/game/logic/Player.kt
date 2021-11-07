package pt.isec.multiplayerreversi.game.logic

import java.io.Serializable

class Player(
    private val piece: Piece,
    private val profile: Profile
) : Serializable {

    constructor(piece: Piece, profile: Profile, hasUsedBomb: Boolean, hasUsedTrade: Boolean)
            : this(piece, profile) {
        this.hasUsedBomb = hasUsedBomb
        this.hasUsedTrade = hasUsedTrade
    }

    companion object {
        private var playerIdCounter = 0
    }

    private var playerId = playerIdCounter++

    private var hasUsedBomb = false
    private var hasUsedTrade = false

    fun getPlayerId() = playerId
    fun getProfile() = profile
    fun getPiece() = piece
    fun hasUsedBomb() = hasUsedBomb
    fun hasUsedTrade() = hasUsedTrade
}