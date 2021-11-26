package pt.isec.multiplayerreversi.game.logic

import android.util.Log
import pt.isec.multiplayerreversi.App.Companion.OURTAG

class Game(
    players: ArrayList<Player>,
    startingPlayer: Player = players.random(),
) {

    private val gameData: GameData

    private var readyPlayers: ArrayList<Int>? = ArrayList()

    init {
        gameData = GameData()
        gameData.sideLength = if (players.size == 2) 8 else 10
        gameData.players = players
        gameData.currentPlayer = startingPlayer
        gameData.board = Array(sideLength) { Array(sideLength) { Piece.Empty } }
        gameData.shouldShowPossibleMoves = true
    }

    init {
        when (players.size) {
            2 -> {
                val p1 = players[0].piece
                board[3][3] = p1
                board[4][4] = p1
                val p2 = players[1].piece
                board[4][3] = p2
                board[3][4] = p2
            }
            3 -> {
                val p1 = players[0].piece
                board[2][4] = p1
                board[3][5] = p1
                board[6][3] = p1
                board[7][2] = p1
                val p2 = players[1].piece
                board[3][4] = p2
                board[2][5] = p2
                board[6][6] = p2
                board[7][7] = p2
                val p3 = players[2].piece
                board[6][2] = p3
                board[7][3] = p3
                board[7][6] = p3
                board[6][7] = p3
            }
            else -> throw IllegalStateException("Games are only allowed with 2 or 3 players")
        }
    }

    fun playerReady(player: Player) {
        if (readyPlayers != null) {
            readyPlayers?.let { ready ->
                if (ready.contains(player.playerId)) {
                    Log.e(OURTAG,
                        "Player that is already ready got ready again : ${player.playerId}")
                    return
                }

                ready.add(player.playerId)
                if (ready.size == players.size) {
                    readyPlayers = null
                    start()
                }
            }
        }
    }

    fun start() {
        updatePossibleMovesForPlayer()
        sendEventsAfterPlay()
    }

    //TODO pass play

    fun playAt(player: Player, line: Int, column: Int): Boolean {
        if (player != currentPlayer)
            return false
        if (!currentPlayerPossibleMoves.contains(Vector(column, line)))
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
                val score = countPieces(it.piece)
                playersStats.add(PlayerEndStats(it, score))

                if (score > highestScore) {
                    highestScoreId = it.playerId
                    highestScore = score
                } else if (score == highestScore) {
                    highestScoreId = -1
                }
            }
            val endStats = GameEndStats(highestScoreId, playersStats)
            players.forEach {
                it.callbacks?.gameFinishedCallback?.let { it(endStats) }
            }
        }
        sendEventsAfterPlay()
    }

    private fun countPieces(piece: Piece): Int {
        var count = 0
        for (column in 0 until sideLength)
            for (line in 0 until sideLength)
                if (board[line][column] == piece)
                    count++
        return count
    }

    private fun checkIfFinished(): Boolean {
        //Se tiver jogadas o jogo não acabou
        if (currentPlayerPossibleMoves.size > 0) return false
        var nextPlayer = currentPlayer
        while (true) {
            // depois vamos ver a todos os outros jogadores se teem uma jogada possível
            nextPlayer = getNext(nextPlayer, players)
            if (nextPlayer == currentPlayer)
                break
            val piece = nextPlayer.piece

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
//        propertyChange.firePropertyChange(updateBoardEvent, null, board)
        players.forEach {
            it.callbacks?.updateBoardCallback?.let { it(board) }
        }
//        propertyChange.firePropertyChange(updateCurrentPlayerEvent, null, currentPlayer.playerId)
        players.forEach {
            it.callbacks?.changePlayerCallback?.let { it(currentPlayer.playerId) }
        }
        if (gameData.shouldShowPossibleMoves) {
//            propertyChange.firePropertyChange(showMovesEvent, null, currentPlayerMoves)
            players.forEach {
                it.callbacks?.possibleMovesCallback?.let { it(currentPlayerPossibleMoves) }
            }
        }
    }

    private fun <T> getNext(curr: T, list: List<T>): T = list[(list.indexOf(curr) + 1) % list.size]

    private fun executePlayAt(line: Int, column: Int): Boolean {
        if (board[line][column] !== Piece.Empty) return false

        val currPlayerPiece = currentPlayer.piece

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
        val piece = currentPlayer.piece
        currentPlayerPossibleMoves = ArrayList(20)
        for (column in 0 until sideLength) {
            for (line in 0 until sideLength) {
                val pos = Vector(column, line)
                if (checkCanPlayAt(piece, pos))
                    currentPlayerPossibleMoves.add(pos)
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
                if (distance <= 1 && pieceHere === player)
                    break
                // se encontrar uma peça do outro lado das peças inimigas
                if (distance > 1 && pieceHere === player)
                    return true
            }
        }
        return false
    }

    fun playBombPiece(player: Player, line: Int, column: Int): Boolean {
        if (board[line][column] != player.piece || player.hasUsedBomb) {
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
        players.forEach {
            it.callbacks?.playerUsedBombCallback?.invoke(currentPlayer.playerId)
        }
        updateState()
        return true
    }

    fun playTrade(player: Player, piece: ArrayList<Vector>) {
        if (player != currentPlayer) return

        val piece1 = piece[0]
        val piece2 = piece[1]
        val opponent = piece[2]
        if (board[piece1.y][piece1.x] != currentPlayer.piece ||
            board[piece2.y][piece2.x] != currentPlayer.piece ||
            board[opponent.y][opponent.x] == currentPlayer.piece
        ) return

        val opponentPiece = board[opponent.y][opponent.x]
        board[piece1.y][piece1.x] = opponentPiece
        board[piece2.y][piece2.x] = opponentPiece
        //board[tradePieces[2].y][tradePieces[2].x] = currentPlayer.piece

        board[opponent.y][opponent.x] = Piece.Empty
        executePlayAt(opponent.y, opponent.x)

        currentPlayer.hasUsedTrade = true
        players.forEach {
            it.callbacks?.playerUsedTradeCallback?.invoke(currentPlayer.playerId)
        }
        updateState()
    }


    val board: Array<Array<Piece>>
        get() = gameData.board

    val players: List<Player>
        get() = gameData.players

    var currentPlayer: Player
        get() = gameData.currentPlayer
        private set(value) {
            gameData.currentPlayer = value
        }

    val sideLength: Int
        get() = gameData.sideLength

    var currentPlayerPossibleMoves: ArrayList<Vector>
        get() = gameData.currentPlayerPossibleMoves
        private set(value) {
            gameData.currentPlayerPossibleMoves = value
        }

    companion object {
        private val directions = arrayOf(
            Vector(-1, -1), Vector(0, -1), Vector(1, -1),
            Vector(-1, 0), /*     Center    */  Vector(1, 0),
            Vector(-1, 1), Vector(0, 1), Vector(1, 1)
        )

        const val PLAYER_LIMIT = 3
    }
}