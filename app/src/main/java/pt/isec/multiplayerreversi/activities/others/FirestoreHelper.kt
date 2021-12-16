package pt.isec.multiplayerreversi.activities.others

import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import pt.isec.multiplayerreversi.activities.HistoryActivity
import pt.isec.multiplayerreversi.game.logic.GameEndStats

class FirestoreHelper(private val id: String) {

    private val db = Firebase.firestore

    private fun getPlayerDocument(): DocumentReference {
        return db.collection("MultiplayerReversi").document(id)
    }

    fun insertData(gameEndStats: GameEndStats, playerScore: Int) {
        val playerStats = gameEndStats.playerStats
        val players: ArrayList<HistoryActivity.Player> = ArrayList(3)
        var topSocore = 0
        playerStats.forEach {
            val player: HistoryActivity.Player =
                HistoryActivity.Player(it.player.profile.name, it.pieces)
            if (it.player.playerId == gameEndStats.winningPlayerId)
                topSocore = it.pieces
            players.add(player)
        }

        //TODO addcionar as imagems dos players
        val game = HistoryActivity.Game(players, playerScore)
        val updatedGames = updateTopScores(game)
        val scores = hashMapOf("games" to updatedGames, "topscore" to topSocore)
        getPlayerDocument().set(scores)
    }

    private fun updateTopScores(newGame: HistoryActivity.Game): MutableList<HistoryActivity.Game> {
        val gamesHistory = getGamesHistory()
        gamesHistory.add(newGame)
        val sortedByDescending =
            gamesHistory.sortedByDescending { it.playerScore }.toMutableList()
        if (sortedByDescending.size > 5) sortedByDescending.removeLast()
        return sortedByDescending
    }


    fun getTopScore(): Int {
        return getPlayerDocument().get().result.get("topscore") as Int
    }

    fun getGamesHistory(): ArrayList<HistoryActivity.Game> {
        val document = Tasks.await(getPlayerDocument().get())
        return if (document != null) document.get("games") as ArrayList<HistoryActivity.Game>
        else ArrayList(5)
    }

}