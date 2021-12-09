package pt.isec.multiplayerreversi.game.logic

import pt.isec.multiplayerreversi.game.interactors.GameCallbacks

class Player(
    var profile: Profile = Profile(),
    var piece: Piece = Piece.Empty,
    var callbacks: GameCallbacks? = null,
    private var hasUsedBomb: Boolean = false,
    private var hasUsedTrade: Boolean = false,
    var playerId: Int = playerIdCounter++,
) {

    var score: Int = 0

    constructor(profile: Profile, piece: Piece, hasUsedBomb: Boolean, hasUsedTrade: Boolean)
            : this(profile, piece) {
        this.hasUsedBomb = hasUsedBomb
        this.hasUsedTrade = hasUsedTrade
    }

    companion object {
        private var playerIdCounter = 0
    }

    override fun toString(): String {
        return "Player(playerId=$playerId, profile=$profile, piece=$piece, hasUsedBomb=$hasUsedBomb, hasUsedTrade=$hasUsedTrade)"
    }

    fun canUseBomb(): Boolean {
        return !hasUsedBomb
    }

    fun canUseTrade(): Boolean {
        return !hasUsedTrade
    }

    fun useBomb() {
        hasUsedBomb = true
    }

    fun useTrade() {
        hasUsedTrade = true
    }
}