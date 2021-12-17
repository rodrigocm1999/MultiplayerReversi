package pt.isec.multiplayerreversi.activities.others

import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import org.json.JSONException
import pt.isec.multiplayerreversi.game.interactors.networking.encodeDrawableToString
import pt.isec.multiplayerreversi.game.logic.GameEndStats

class FirestoreHelper(private val id: String) {

    private val db = Firebase.firestore

    private fun getPlayerDocument(): DocumentReference {
        return db.collection("MultiplayerReversi").document(id)
    }

    fun insertData(gameEndStats: GameEndStats, playerScore: Int) {
        val playerStats = gameEndStats.playerStats
        val players: ArrayList<Player> = ArrayList(3)
        var topScore = getGamesHistory().topscore
        playerStats.forEach {
            val imgString = it.player.profile.icon?.let { it1 -> encodeDrawableToString(it1) }
            val player = Player(it.player.profile.name, it.pieces,imgString)
            if (it.player.playerId == gameEndStats.winningPlayerId && topScore < playerScore)
                topScore = it.pieces
            players.add(player)
        }
        val game = Game(players, playerScore)
        val updatedGames = updateTopScores(game)
        val scores = hashMapOf("games" to updatedGames, "topscore" to topScore)
        getPlayerDocument().set(scores)
    }

    private fun updateTopScores(newGame: Game): MutableList<Game> {
        val gamesHistory = getGamesHistory()
        val games = gamesHistory.games
        games.add(newGame)
        val sortedByDescending =
            games.sortedByDescending { it.playerScore }.toMutableList()
        if (sortedByDescending.size > 5) sortedByDescending.removeLast()
        return sortedByDescending
    }


    fun getTopScore(): Int {
        return getGamesHistory().topscore
    }

    fun getGamesHistory(): History {
        val document = Tasks.await(getPlayerDocument().get())
        val result = document.toObject<History?>()
        if (result != null) return result
        return History()
    }

    class Player(
        var name: String = "",
        var score: Int = 0,
        var imgString: String? = null
    )

    class Game(
        var players: ArrayList<Player> = ArrayList(0),
        var playerScore: Int = 0,
    )

    class History(
        var games: ArrayList<Game> = ArrayList(0),
        var topscore: Int = 0,
    )

}