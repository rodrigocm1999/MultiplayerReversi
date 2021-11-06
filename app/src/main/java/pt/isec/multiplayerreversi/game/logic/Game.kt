package pt.isec.multiplayerreversi.game.logic

class Game(
    private val sideLength: Int,
    private val players: List<Player>,
    startingPlayer: Player,
) {

    private val board =
        Array(sideLength) { Array(sideLength) { Piece.Empty } } // gameArea[line][column]
    private val currentPlayer: Player = startingPlayer
    private lateinit var possibleMoves: ArrayList<Vector>

    init {
        when (players.size) {
            2 -> {
                val p1 = players[0].getPiece()
                board[3][3] = p1
                board[4][4] = p1
                val p2 = players[1].getPiece()
                board[4][3] = p2
                board[3][4] = p2
            }
            3 -> {
                val p1 = players[0].getPiece()
                board[2][4] = p1
                board[3][5] = p1
                board[6][3] = p1
                board[7][2] = p1
                val p2 = players[1].getPiece()
                board[3][4] = p2
                board[2][5] = p2
                board[6][6] = p2
                board[7][7] = p2
                val p3 = players[2].getPiece()
                board[6][2] = p3
                board[7][3] = p3
                board[7][6] = p3
                board[6][7] = p3
            }
            else -> throw IllegalStateException("Games are only allowed with 2 or 3 players")
        }
    }

    private val directions = arrayOf(
        Vector(-1, -1), Vector(0, -1), Vector(1, -1),
        Vector(-1, 0), /*     Center    */  Vector(1, 0),
        Vector(-1, 1), Vector(0, 1), Vector(1, 1)
    )

    fun playAt(line: Int, column: Int) {

        if (board[line][column] != Piece.Empty) return

        val currPlayerPiece = currentPlayer.getPiece()

        for (offset in directions) {
            val currPos = Vector(column, line)
            var distance = 0
            var foundPieceToConnect = false

            while (!foundPieceToConnect) {
                currPos.add(offset)
                distance++

                if (currPos.x < 0 || currPos.x >= sideLength
                    || currPos.y < 0 || currPos.y >= sideLength
                ) break

                val pieceHere = board[currPos.y][currPos.x]
                if (pieceHere == Piece.Empty)
                    break
                if (pieceHere == currPlayerPiece)
                    foundPieceToConnect = true
            }
            if (foundPieceToConnect) {
                // from the piece that was found start going back and change all pieces
                while (distance > 1) { // > 1 To not go over the starting piece again
                    currPos.sub(offset)
                    distance--
                    board[currPos.y][currPos.x] = currPlayerPiece
                }
            }
        }
    }

    fun getPossibleMovesForPlayer(player: Piece): ArrayList<Vector> {
        val possibleMoves = ArrayList<Vector>(20)
        for (column in 0 until sideLength) {
            for (line in 0 until sideLength) {
                val pos = Vector(column, line)
                if (checkCanPlayAt(player, pos))
                    possibleMoves.add(pos)
            }
        }
        return possibleMoves
    }

    private fun checkCanPlayAt(player: Piece, position: Vector): Boolean {
        if (board[position.y][position.x] != Piece.Empty) return false
        //from position go in every direction
        for (offset in directions) {
            val currPos = Vector(position.x, position.y)
            var distance = 0
            //Go until break or return
            while (true) {
                currPos.add(offset)
                distance++
                //Não sair do mapa
                if (currPos.x < 0 || currPos.x >= sideLength
                    || currPos.y < 0 || currPos.y >= sideLength
                ) break
                //No caso de deixar de haver uma peça, esta direção já não interessa
                if (board[currPos.y][currPos.x] == Piece.Empty)
                    break
                // se encontrar uma peça do outro lado das peças inimigas
                if (distance > 1 && board[currPos.y][currPos.x] == player)
                    return true
            }
        }
        return false
    }

    fun getBoard() = board
}