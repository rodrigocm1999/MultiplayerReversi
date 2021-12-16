package pt.isec.multiplayerreversi.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import pt.isec.multiplayerreversi.App
import pt.isec.multiplayerreversi.databinding.ActivityHistoryBinding
import pt.isec.multiplayerreversi.game.logic.GameEndStats

class HistoryActivity : AppCompatActivity() {

    private lateinit var binding : ActivityHistoryBinding
    private lateinit var app: App
    private lateinit var db: FirebaseFirestore
    private lateinit var games : ArrayList<Game>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        app = application as App

        db = Firebase.firestore

        //TODO ir fazer o layout + fazer aparecer

    }

    private fun readData(){
        val get =
            db.collection("MultiplayerReversi").document(app.getProfile().email!!).get()

        val result = get.getResult()
        val topScore = result.get("topscore") as Int
        games = result.get("games") as ArrayList<Game>
        var ola = true

    }

    private fun insertData(db : FirebaseFirestore, gameEndStats: GameEndStats,playerScore : Int){
        val playerStats = gameEndStats.playerStats
        var players : ArrayList<Player> = ArrayList(3)
        playerStats.forEach {
            var player : Player = Player(it.player.profile.name,it.pieces)
            players.add(player)
        }
        var game =  Game(players,playerScore)
        val scores = hashMapOf(
            "topscore" to 0,
            "games" to arrayOf(game)
        )
        db.collection("MultiplayerReversi").document(app.getProfile().email!!).set(scores)
    }
    class Player(
        var name : String,
        var score : Int
    )
    class Game(
        var players: ArrayList<Player>? = null,
        var playerScore : Int
    )
}