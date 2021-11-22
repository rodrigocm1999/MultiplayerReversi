package pt.isec.multiplayerreversi.game.logic

class GameData {

    var sideLength: Int = 8
    lateinit var board: Array<Array<Piece>>//Array(sideLength) { Array(sideLength) { Piece.Empty } } // board[line][column]
    lateinit var players: ArrayList<Player>
    lateinit var currentPlayer: Player
    lateinit var currentPlayerMoves: ArrayList<Vector>

    var shouldShowPossibleMoves = true

}