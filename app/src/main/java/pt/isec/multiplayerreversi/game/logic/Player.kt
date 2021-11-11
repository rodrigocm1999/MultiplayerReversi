package pt.isec.multiplayerreversi.game.logic

class Player(
    profile: Profile = Profile(),
    piece: Piece = Piece.Empty,
) {

    constructor(profile: Profile, piece: Piece, hasUsedBomb: Boolean, hasUsedTrade: Boolean)
            : this(profile, piece) {
        this.hasUsedBomb = hasUsedBomb
        this.hasUsedTrade = hasUsedTrade
    }

    companion object {
        private var playerIdCounter = 0
    }

    var playerId = playerIdCounter++

    var profile = profile
    var piece = piece
    var hasUsedBomb = false
    var hasUsedTrade = false
    override fun toString(): String {
        return "Player(playerId=$playerId, profile=$profile, piece=$piece, hasUsedBomb=$hasUsedBomb, hasUsedTrade=$hasUsedTrade)"
    }


}