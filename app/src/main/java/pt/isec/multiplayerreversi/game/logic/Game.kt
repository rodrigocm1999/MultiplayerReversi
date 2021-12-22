package pt.isec.multiplayerreversi.game.logic

import android.util.Log
import pt.isec.multiplayerreversi.App
import pt.isec.multiplayerreversi.App.Companion.OURTAG
import java.util.*
import kotlin.collections.ArrayList

class Game(val gameData: GameData) {

    private var countPasses: Int = 0

    private var readyPlayers: ArrayList<Int>? = ArrayList()

    init {
        if (!gameData.boardIsReady())
            gameData.board = prepareBoard(players)

    }

    private fun prepareBoard(players: ArrayList<Player>): Array<Array<Piece>> {
        val board = Array(sideLength) { Array(sideLength) { Piece.Empty } }
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
        return board
    }

    fun playerReady(player: Player) {
        readyPlayers?.let { ready ->
            if (ready.contains(player.playerId)) {
                Log.e(OURTAG, "Player that is already ready got ready again : ${player.playerId}")
                return
            }

            ready.add(player.playerId)
            if (ready.size == players.size) {
                readyPlayers = null
                startGame()
            }
        }
    }

    private fun startGame() {
        Log.i(OURTAG, "Jogo começou")
        updatePossibleMovesForPlayer()
        sendEventsAfterPlay()
    }

    fun passPlayer(player: Player) {
        if (player !== currentPlayer) return
        countPasses++
        updateState()
    }

    fun playAt(player: Player, line: Int, column: Int): Boolean {
        if (player != currentPlayer)
            return false
        if (!currentPlayerPossibleMoves.contains(Vector(column, line)))
            return false
        if (!executePlayAt(line, column))
            return false
        updateState()
        countPasses = 0
        return true
    }

    private fun updateState() {
        currentPlayer = getNext(currentPlayer, players)
        updatePossibleMovesForPlayer()
        updateScores(players)

        if (checkIfFinished()) {
            val playersStats = ArrayList<PlayerEndStats>(players.size)
            var highestScoreId = -1
            var highestScore = -1
            val boardStats = getBoardStats()
            players.forEach { player ->
                val score = boardStats.pbs[player.piece]!!.piecesCount
                playersStats.add(PlayerEndStats(player, score))

                if (score > highestScore) {
                    highestScoreId = player.playerId
                    highestScore = score
                } else if (score == highestScore)
                    highestScoreId = -1
            }
            val endStats = GameEndStats(highestScoreId, playersStats)
            players.forEach {
                it.callbacks?.updateBoardCallback?.let { it(board) }
                it.callbacks?.gameFinishedCallback?.let { it(endStats) }
            }
            return
        }
        sendEventsAfterPlay()
    }

    private fun updateScores(players: ArrayList<Player>) {
        val boardStats = getBoardStats()
        players.forEach {
            it.score = boardStats.pbs[it.piece]!!.piecesCount
        }
    }

    private fun checkIfFinished(): Boolean {
        //Se todos os jogadores fizerem pass acaba
        if (countPasses >= players.size) return true

        val boardStats = getBoardStats()
        //Se o tabuleiro estiver cheio
        if (boardStats.totalPieces == sideLength * sideLength) return true
        //Se o tabuleiro estiver vazio
        if (boardStats.totalPieces == 0) return true
        //Se tiver jogadas o jogo não acabou
        if (currentPlayerPossibleMoves.size > 0) return false
        //Se um player for o unico com peças
        if (boardStats.pbs.any { entry -> entry.value.piecesCount == boardStats.totalPieces }) return true
        // Se ainda poder jogar alguma jogada especial
        var playerPieces = boardStats.pbs[currentPlayer.piece]!!.piecesCount
        if (playerPieces >= 1 && currentPlayer.canUseBomb() ||
            playerPieces >= 2 && currentPlayer.canUseTrade()
        ) return false

        var nextPlayer = currentPlayer
        while (true) {
            // depois vamos ver a todos os outros jogadores se teem uma jogada possível
            nextPlayer = getNext(nextPlayer, players)
            if (nextPlayer === currentPlayer)
                break

            // Se ainda poder jogar alguma jogada especial
            playerPieces = boardStats.pbs[nextPlayer.piece]!!.piecesCount
            if (playerPieces >= 1 && nextPlayer.canUseBomb() ||
                playerPieces >= 2 && nextPlayer.canUseTrade()
            ) return false

            // Se conseguir jogar pelo menos em 1 sitio quer dizer que o jogo ainda não acabou
            val piece = nextPlayer.piece
            for (column in 0 until sideLength)
                for (line in 0 until sideLength)
                    if (checkCanPlayAt(piece, Vector(column, line))) return false
        }
        // se não encontrar jogadas possíveis, o jogo tem de terminar
        return true
    }


//    private fun countPieces(piece: Piece): Int {
//        var count = 0
//        for (column in 0 until sideLength)
//            for (line in 0 until sideLength)
//                if (board[line][column] == piece)
//                    count++
//        return count
//    }

//    private fun playerPieces(playerPiece: Piece): Int {
//        var count = 0
//        for (column in 0 until sideLength)
//            for (line in 0 until sideLength)
//                if (board[line][column] == playerPiece)
//                    count++
//        return count
//    }

//    private fun boardIsFull(boardStats: BoardStats): Boolean {
//        for (column in 0 until sideLength)
//            for (line in 0 until sideLength)
//                if (board[line][column] == Piece.Empty)
//                    return false
//        return true
//    }

//    private fun boardIsEmpty(boardStats: BoardStats): Boolean {
//        for (column in 0 until sideLength)
//            for (line in 0 until sideLength)
//                if (board[line][column] != Piece.Empty)
//                    return false
//        return true
//    }

    data class BoardStats(
        val pbs: TreeMap<Piece, PlayerBoardStats> = TreeMap(), var totalPieces: Int = 0,
    )

    data class PlayerBoardStats(val piece: Piece, var piecesCount: Int = 0)

    private fun getBoardStats(): BoardStats {
        val boardStats = BoardStats()
        players.forEach { boardStats.pbs[it.piece] = PlayerBoardStats(it.piece) }

        for (column in 0 until sideLength) {
            for (line in 0 until sideLength) {
                val playerStats = boardStats.pbs[board[line][column]]
                playerStats?.let { it.piecesCount++ }
            }
        }

        boardStats.pbs.forEach { boardStats.totalPieces += it.value.piecesCount }
        return boardStats
    }

    private fun sendEventsAfterPlay() {
        players.forEach {
            it.callbacks?.updateBoardCallback?.let {
                it(board)
                updateScores(players)
            }
            it.callbacks?.changePlayerCallback?.let { it(currentPlayer.playerId) }
            it.callbacks?.possibleMovesCallback?.let { it(currentPlayerPossibleMoves) }
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

    fun playBombPiece(player: Player, line: Int, column: Int) {
        if (player != currentPlayer) return
        if (!player.canUseBomb() || board[line][column] != player.piece) return

        board[line][column] = Piece.Empty
        for (offset in directions) {
            val currPos = Vector(column, line)

            currPos.add(offset)
            if (currPos.x < 0 || currPos.x >= sideLength
                || currPos.y < 0 || currPos.y >= sideLength
            ) continue
            board[currPos.y][currPos.x] = Piece.Empty

        }
        player.useBomb()
        players.forEach { it.callbacks?.playerUsedBombCallback?.invoke(currentPlayer.playerId) }
        updateState()
        countPasses = 0
    }

    fun playTrade(player: Player, piece: ArrayList<Vector>) {
        if (player != currentPlayer) return
        if (!player.canUseTrade()) return

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

        currentPlayer.useTrade()
        players.forEach { it.callbacks?.playerUsedTradeCallback?.invoke(currentPlayer.playerId) }
        updateState()
        countPasses = 0
    }

    fun playerLeaving(ownPlayer: Player) {
        players.forEach {
            if (it.playerId != ownPlayer.playerId)
                it.callbacks?.gameTerminatedCallback?.invoke()
        }
    }


    val board: Array<Array<Piece>>
        get() = gameData.board

    val players: ArrayList<Player>
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

    val gameSettings: App.GameSettings
        get() = gameData.gameSettings

    companion object {
        private val directions = arrayOf(
            Vector(-1, -1), Vector(0, -1), Vector(1, -1),
            Vector(-1, 0), /*     Center    */  Vector(1, 0),
            Vector(-1, 1), Vector(0, 1), Vector(1, 1)
        )

        const val PLAYER_LIMIT = 3
    }
}