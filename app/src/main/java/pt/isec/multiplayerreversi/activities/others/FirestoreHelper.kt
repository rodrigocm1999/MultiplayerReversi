package pt.isec.multiplayerreversi.activities.others

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import pt.isec.multiplayerreversi.activities.HistoryActivity
import pt.isec.multiplayerreversi.game.logic.GameEndStats
import java.lang.Exception

class FirestoreHelper() {

    val db = Firebase.firestore



    private fun getPlayerDocument(email : String) : DocumentReference? {
        return db.collection("MultiplayerReversi").document(email)
    }

    fun insertData(email: String, gameEndStats: GameEndStats,playerScore : Int){
        val playerStats = gameEndStats.playerStats
        var players : ArrayList<HistoryActivity.Player> = ArrayList(3)
        var topSocore = 0
        playerStats.forEach {
            var player : HistoryActivity.Player =
                HistoryActivity.Player(it.player.profile.name, it.pieces)
            if (it.player.playerId == gameEndStats.winningPlayerId){
                topSocore = it.pieces
            }
            players.add(player)
        }

        //TODO addcionar as imagems dos players
        var game = HistoryActivity.Game(players,playerScore)
        val updatedGames = updateTopScores(email, newGame = game)
        val scores = hashMapOf("games" to updatedGames, "topscore" to topSocore)
        getPlayerDocument(email)?.set(scores)
    }
    private fun updateTopScores(email: String, newGame : HistoryActivity.Game): MutableList<HistoryActivity.Game> {
        val gamesHistory = getGamesHistory(email)
        gamesHistory.add(newGame)
        val sortedByDescending = gamesHistory.sortedByDescending { it.playerScore }
        if (sortedByDescending.size > 5){
            val toMutableList = sortedByDescending.toMutableList()
            toMutableList.removeLast()
            return toMutableList
        }
        return sortedByDescending.toMutableList()
    }



    fun getTopScore(email: String) : Int{
        return getPlayerDocument(email)!!.get().result.get("topscore") as Int
    }
    fun getGamesHistory(email: String) : ArrayList<HistoryActivity.Game> {
        return getPlayerDocument(email)!!.get().result.get("games") as ArrayList<HistoryActivity.Game>
    }

}