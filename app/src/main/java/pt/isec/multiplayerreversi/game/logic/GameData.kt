package pt.isec.multiplayerreversi.game.logic

class GameData {

    var sideLength: Int = 8
    lateinit var board: Array<Array<Piece>>
    lateinit var players: ArrayList<Player>
    lateinit var currentPlayer: Player
    lateinit var currentPlayerMoves: ArrayList<Vector>

    var shouldShowPossibleMoves: Boolean = true

}