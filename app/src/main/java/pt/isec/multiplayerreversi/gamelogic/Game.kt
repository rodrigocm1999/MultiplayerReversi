package pt.isec.multiplayerreversi.gamelogic

class Game(players: List<Player>, startingPlayer: Player, width: Int, height: Int) {

    private val gameArea = Array(height) { Array(width) { Piece.Empty } }
    private val width = width
    private val height = height
    private val players: List<Player> = players
    private val currentPlayer: Player = startingPlayer
    private lateinit var possibleMoves: ArrayList<Vector>

    private val directions = arrayOf(
        Vector(-1, -1), Vector(0, -1), Vector(1, -1),
        Vector(-1, 0), /*     Center    */  Vector(1, 0),
        Vector(-1, 1), Vector(0, 1), Vector(1, 1)
    )

    private fun playAt(position: Vector) {
        if (gameArea[position.y][position.x] != Piece.Empty) return

        val currPlayerPiece = currentPlayer.getPiece()

        for (offset in directions) {
            val currPos = Vector(position.x, position.y)
            var distance = 0
            var foundPieceToConnect = false

            while (!foundPieceToConnect) {
                currPos.add(offset)
                distance++

                if (currPos.x < 0 || currPos.x >= width
                    || currPos.y < 0 || currPos.y >= height
                ) break

                if(gameArea[currPos.y][currPos.x] == Piece.Empty)
                    break

                if (gameArea[currPos.y][currPos.x] == currPlayerPiece)
                    foundPieceToConnect = true
            }
            if (foundPieceToConnect) {
                // from the piece that was found start going back and change all pieces
                while (distance > 1) { // > 1 To not go over the starting piece again
                    currPos.sub(offset)
                    distance--
                    gameArea[currPos.y][currPos.x] = currPlayerPiece
                }
            }
        }
    }

    private fun getPossibleMovesForPlayer(player: Piece): ArrayList<Vector> {
        val possibleMoves = ArrayList<Vector>(20)

        for (column in 0 until width) {
            for (line in 0 until height) {
                val pos = Vector(column, line)

                if (checkCanPlayAt(player, pos))
                    possibleMoves.add(pos)
            }
        }
        return possibleMoves
    }

    private fun checkCanPlayAt(player: Piece, position: Vector): Boolean {
        if (gameArea[position.y][position.x] != Piece.Empty) return false
        //from position go in every direction
        for (offset in directions) {
            val currPos = Vector(position.x, position.y)
            var distance = 0
            //Go until break or return
            while (true) {
                currPos.add(offset)
                distance++
                //Não sair do mapa
                if (currPos.x < 0 || currPos.x >= width
                    || currPos.y < 0 || currPos.y >= height
                ) break
                //No caso de deixar de haver uma peça, esta direção já não interessa
                if (gameArea[currPos.y][currPos.x] == Piece.Empty)
                    break
                // se encontrar uma peça do outro lado das peças inimigas
                if (distance > 1 && gameArea[currPos.y][currPos.x] == player)
                    return true
            }
        }
        return false
    }

}