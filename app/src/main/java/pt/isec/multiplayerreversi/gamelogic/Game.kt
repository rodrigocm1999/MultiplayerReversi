package pt.isec.multiplayerreversi.gamelogic

class Game(players: List<Player>, startingPlayer: Player, width: Int, height: Int) {

    private val gameArea: Array<Array<Piece?>> = Array(height) { Array(width) { null } }
    private val width = width
    private val height = height
    private val players: List<Player> = players
    private val currentPlayer: Player = startingPlayer

    private val directions = arrayOf(
        Point(-1, -1), Point(0, -1), Point(1, -1),
        Point(-1, 0),  /*     Center    */Point(1, 0),
        Point(-1, 1), Point(0, 1), Point(1, 1)
    )

    private fun playAt(position: Point) {
        if (gameArea[position.y][position.x] != null) return

        val currPlayerPiece = currentPlayer.getPiece()

        for (offset in directions) {
            val currPos = Point(position.x, position.y)
            var distance = 0
            var foundPieceToConnect = false

            while (!foundPieceToConnect) {
                currPos.add(offset)
                distance++

                if (currPos.x < 0 || currPos.x >= width || currPos.y < 0 || currPos.y >= height)
                    break
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