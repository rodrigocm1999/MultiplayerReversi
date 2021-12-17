package pt.isec.multiplayerreversi.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.get
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import pt.isec.multiplayerreversi.App
import pt.isec.multiplayerreversi.R
import pt.isec.multiplayerreversi.activities.others.FirestoreHelper
import pt.isec.multiplayerreversi.databinding.ActivityHistoryBinding
import pt.isec.multiplayerreversi.game.interactors.networking.decodeDrawableFromString
import pt.isec.multiplayerreversi.game.logic.GameEndStats
import pt.isec.multiplayerreversi.game.logic.Player
import kotlin.concurrent.thread

class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding
    private lateinit var app: App
    private lateinit var db: FirestoreHelper
    var history = FirestoreHelper.History()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        app = application as App

        db = FirestoreHelper(app.getProfile().email!!)

        //TODO testar melhor isto e talvez fazer alguns ajustos graficos

        updateView()
        threadUpdateHistory()

    }

    private fun threadUpdateHistory() {
        thread {
            history = db.getGamesHistory()
            runOnUiThread {
                updateView()
            }
        }
    }

    private fun updateView() {
        binding.tvTopScore.text = history.topscore.toString()
        setupGamesViews(history.games)
    }


    private fun setupGamesViews(games: ArrayList<FirestoreHelper.Game>) {
        if (games.size > 0) {
            val defaultAvatar = AppCompatResources.getDrawable(this, R.drawable.avatar_icon)!!
            var listGamesViews = ArrayList<RowGameView>(0)

            games.forEach { game ->
                val linearLayoutGames = layoutInflater.inflate(
                    R.layout.history_row_game_layout, binding.layoutHistoryGames
                ) as ViewGroup
                val parentViewGames = linearLayoutGames[linearLayoutGames.childCount - 1]
                val rowGameView = RowGameView(
                    llHistoryRow = parentViewGames.findViewById(R.id.llHistoryRow)
                )

                game.players.forEach { player ->
                    var listPlayersView = ArrayList<PlayerHistoryStatsView>(2)
                    val linearLayoutPlayers = layoutInflater.inflate(
                        R.layout.history_player_stats, rowGameView.llHistoryRow
                    ) as ViewGroup
                    val parentView = linearLayoutPlayers[linearLayoutPlayers.childCount - 1]
                    val playerHistoryStatsView = PlayerHistoryStatsView(
                        imgViewHistoryPlayer = parentView.findViewById(R.id.imgViewHistoryPlayer),
                        tvHistoryPlayerName = parentView.findViewById(R.id.tvHistoryPlayerName),
                        tvHistoryPlayerScore = parentView.findViewById(R.id.tvHistoryPlayerScore)
                    )
                    listPlayersView.add(playerHistoryStatsView)
                    playerHistoryStatsView.tvHistoryPlayerName.text = player.name
                    playerHistoryStatsView.tvHistoryPlayerScore.text = player.score.toString()
                    if (player.imgString != null){
                        val decodeDrawableFromString = decodeDrawableFromString(player.imgString!!)
                        playerHistoryStatsView.imgViewHistoryPlayer.setImageDrawable(decodeDrawableFromString)
                    }else playerHistoryStatsView.imgViewHistoryPlayer.setImageDrawable(defaultAvatar)

                }
                listGamesViews.add(rowGameView)

            }
        }
    }

    data class RowGameView(
        val llHistoryRow: LinearLayout
    )

    data class PlayerHistoryStatsView(
        val imgViewHistoryPlayer: ImageView,
        val tvHistoryPlayerName: TextView,
        val tvHistoryPlayerScore: TextView,
    )

}