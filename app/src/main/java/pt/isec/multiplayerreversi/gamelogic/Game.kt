package pt.isec.multiplayerreversi.gamelogic

class Game(players: List<Player>, startingPlayer: Player, width: Int, height: Int) {

    private val gameArea: Array<Array<Piece?>> = Array(height) { Array(width) { null } }
    private val width = width
    private val height = height
    private val players: List<Player> = players
    private val currentPlayer: Player = startingPlayer

    private val directions = arrayOf(
        Vector(-1, -1), Vector(0, -1), Vector(1, -1),
        Vector(-1, 0),  /*     Center    */Vector(1, 0),
        Vector(-1, 1), Vector(0, 1), Vector(1, 1)
    )

    private fun playAt(position: Vector) {
        if (gameArea[position.y][position.x] != null) return

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
                    || gameArea[currPos.y][currPos.x] == null
                ) break

                if (gameArea[currPos.y][currPos.x] == currPlayerPiece)
                    foundPieceToConnect = true
            }
            if (foundPieceToConnect) {
                while (distance > 0) {
                    currPos.sub(offset)
                    distance--
                    gameArea[currPos.y][currPos.x] = currPlayerPiece
                }
            }
        }
    }

}