package pt.isec.multiplayerreversi.game.logic

import pt.isec.multiplayerreversi.App

class GameData(
    var sharedGameSettings: App.GameSettings?,
    var players: ArrayList<Player>,
) {

    var sideLength: Int = if (players.size == 2) 8 else 10
    var currentPlayer: Player = players.random()
    var currentPlayerPossibleMoves: ArrayList<Vector> = ArrayList()
    var gameSettings: App.GameSettings =
        if (sharedGameSettings != null) sharedGameSettings!! else App.GameSettings()

    private var _board: Array<Array<Piece>>? = null
    var board: Array<Array<Piece>>
        get() = _board!!
        set(value) {
            _board = value
        }

    fun boardIsReady(): Boolean {
        return _board != null
    }

    fun getPlayer(id: Int): Player? {
        return players.find { p -> p.playerId == id }
    }
}