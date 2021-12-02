package pt.isec.multiplayerreversi.game.logic

class GameData {

    var sideLength: Int = 8
    lateinit var board: Array<Array<Piece>>
    lateinit var players: ArrayList<Player>
    lateinit var currentPlayer: Player
    var currentPlayerPossibleMoves: ArrayList<Vector> = ArrayList()

    var shouldShowPossibleMoves: Boolean = true


    override fun toString(): String {
        return "GameData(sideLength=$sideLength, players=$players, currentPlayer=$currentPlayer, currentPlayerMoves=$currentPlayerPossibleMoves, shouldShowPossibleMoves=$shouldShowPossibleMoves)"
    }

    fun getPlayer(id: Int): Player? {
        return players.find { p -> p.playerId == id }
    }
}