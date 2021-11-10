package pt.isec.multiplayerreversi.game.logic

data class GameEndStats(val winningPlayerId: Int, val playerStats: List<PlayerEndStats>)

data class PlayerEndStats(val player: Player, val pieces: Int)