package pt.isec.multiplayerreversi.game.logic

import java.beans.PropertyChangeListener
import java.beans.PropertyChangeSupport

class Game(
    private val sideLength: Int,
    private val players: List<Player>,
    startingPlayer: Player,
) {

    private val board =
        Array(sideLength) { Array(sideLength) { Piece.Empty } } // board[line][column]
    private var currentPlayer = startingPlayer
    private lateinit var currentPlayerMoves: ArrayList<Vector>
    private var shouldShowPossibleMoves = true

    private val propertyChange = PropertyChangeSupport(this)

    companion object {
        const val gameFinishedEvent = "gameFinished"
        const val updateBoardEvent = "updateBoard"
        const val showMovesEvent = "showMoves"
        const val updateCurrentPlayerEvent = "updatePlayer"

        private val directions = arrayOf(
            Vector(-1, -1), Vector(0, -1), Vector(1, -1),
            Vector(-1, 0), /*     Center    */  Vector(1, 0),
            Vector(-1, 1), Vector(0, 1), Vector(1, 1)
        )
    }
    //TODO 10 add more events


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

    fun start() {
        updatePossibleMovesForPlayer()
        sendEventsAfterPlay()
    }


    fun playAt(player: Player, line: Int, column: Int): Boolean {
        if (player != currentPlayer)
            return false
        if (!currentPlayerMoves.contains(Vector(column, line)))
            return false
        if (!executePlayAt(line, column))
            return false
        updateState()
        return true
    }

    private fun updateState() {
        currentPlayer = getNext(currentPlayer, players)
        updatePossibleMovesForPlayer()
        //TODO 2 need to be able to ask to pass to the next player when there is no possible move for this player
        if (checkIfFinished()) {
            val playersStats = ArrayList<PlayerEndStats>(players.size)
            var highestScoreId = -1
            var highestScore = -1
            players.forEach {
                val score = countPieces(it.getPiece())
                playersStats.add(PlayerEndStats(it, score))

                if (score > highestScore) {
                    highestScoreId = it.getPlayerId()
                    highestScore = score
                } else if (score == highestScore) {
                    highestScoreId = -1
                }
            }
            val endStats = GameEndStats(highestScoreId, playersStats)
            propertyChange.firePropertyChange(gameFinishedEvent, null, endStats)
        }
        sendEventsAfterPlay()
    }

    private fun countPieces(piece: Piece): Int {
        var count = 0
        for (column in 0 until sideLength) {
            for (line in 0 until sideLength) {
                if (board[line][column] == piece)
                    count++
            }
        }
        return count
    }

    private fun checkIfFinished(): Boolean {
        //Se tiver jogadas o jogo não acabou
        if (currentPlayerMoves.size > 0) return false
        var nextPlayer = getCurrentPlayer()
        while (true) {
            // depois vamos ver a todos os outros jogadores se teem uma jogada possível
            nextPlayer = getNext(nextPlayer, players)
            if (nextPlayer == currentPlayer)
                break
            val piece = nextPlayer.getPiece()

            for (column in 0 until sideLength) {
                for (line in 0 until sideLength) {
                    val pos = Vector(column, line)
                    // Se conseguir jogar pelo menos em 1 sitio quer dizer que o jogo ainda não acabou
                    if (checkCanPlayAt(piece, pos))
                        return false
                }
            }
        }
        // se não encontrar jogadas possíveis, o jogo tem de terminar
        return true
    }

    private fun sendEventsAfterPlay() {
        propertyChange.firePropertyChange(updateBoardEvent, null, board)
        propertyChange
            .firePropertyChange(updateCurrentPlayerEvent, null, currentPlayer.getPlayerId())
        if (shouldShowPossibleMoves)
            propertyChange.firePropertyChange(showMovesEvent, null, currentPlayerMoves)
    }

    private fun <T> getNext(curr: T, list: List<T>): T = list[(list.indexOf(curr) + 1) % list.size]

    private fun executePlayAt(line: Int, column: Int): Boolean {
        if (board[line][column] != Piece.Empty) return false

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
        board[line][column] = currPlayerPiece
        return true
    }

    private fun updatePossibleMovesForPlayer() {
        val piece = currentPlayer.getPiece()
        currentPlayerMoves = ArrayList(20)
        for (column in 0 until sideLength) {
            for (line in 0 until sideLength) {
                val pos = Vector(column, line)
                if (checkCanPlayAt(piece, pos))
                    currentPlayerMoves.add(pos)
            }
        }
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

                val pieceHere = board[currPos.y][currPos.x]
                //No caso de deixar de haver uma peça, esta direção já não interessa
                if (pieceHere == Piece.Empty)
                    break
                //No caso da primeira peça ser do player quer dizer que não vai saltar por cima de nenhuma
                if (distance <= 1 && pieceHere == player)
                    break
                // se encontrar uma peça do outro lado das peças inimigas
                if (distance > 1 && pieceHere == player)
                    return true
            }
        }
        return false
    }

    fun playBombPiece(player: Player, line: Int, column: Int): Boolean {
        if (board[line][column] != player.getPiece() || player.hasUsedBomb) {
            return false
        }
        board[line][column] = Piece.Empty
        for (offset in directions) {
            val currPos = Vector(column, line)

            currPos.add(offset)
            if (currPos.x < 0 || currPos.x >= sideLength
                || currPos.y < 0 || currPos.y >= sideLength
            ) continue
            board[currPos.y][currPos.x] = Piece.Empty

        }
        player.hasUsedBomb = true
        updateState()
        return true
    }

    fun getSideLength() = sideLength
    fun getCurrentPlayer() = currentPlayer
    fun getPlayers() = players

    fun registerListener(event: String, listener: PropertyChangeListener) =
        propertyChange.addPropertyChangeListener(event, listener)

    fun removeListener(listener: PropertyChangeListener) =
        propertyChange.removePropertyChangeListener(listener)
}