package pt.isec.multiplayerreversi.activities

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import pt.isec.multiplayerreversi.App
import pt.isec.multiplayerreversi.App.Companion.OURTAG
import pt.isec.multiplayerreversi.R
import pt.isec.multiplayerreversi.databinding.ActivityGameBinding
import pt.isec.multiplayerreversi.game.GameGrid
import pt.isec.multiplayerreversi.game.interactors.GamePlayer
import pt.isec.multiplayerreversi.game.logic.GameEndStats
import pt.isec.multiplayerreversi.game.logic.Vector
import kotlin.concurrent.thread

class GameActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGameBinding

    data class PlayerView(val playerId: Int, val view: View)

    private lateinit var playersView: List<PlayerView>
    private var lastPlayerView: PlayerView? = null

    private var clearPossibleMoves: List<Vector>? = null
    private lateinit var proxy: GamePlayer
    private lateinit var gameLayout: GameGrid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)

        val app = application as App
        proxy = app.gamePlayer
            ?: throw IllegalStateException("InteractionSender from App is null when entering the game activity")

        gameLayout = GameGrid(this, binding.gridContainer, displayMetrics,
            layoutInflater, proxy.getGameSideLength(), proxy)

        setCallbacks()
        setListeners()

        proxy.ready()
    }

    override fun onStart() {
        super.onStart()
        gameLayout.refreshBoard()
    }

    private fun setListeners() {
        binding.btnBombPiece.setOnClickListener {
            when {
                proxy.getOwnPlayer().hasUsedBomb -> {
                    Toast.makeText(this, R.string.already_used_bomb_piece, Toast.LENGTH_SHORT)
                        .show()
                }
                gameLayout.isUsingBombPiece -> {
                    gameLayout.showPossibleMoves()
                    gameLayout.isUsingBombPiece = false
                }
                else -> {
                    clearPossibleMoves = gameLayout.clearPossibleMoves()
                    gameLayout.isUsingBombPiece = true
                }
            }
        }

        binding.btnTradePiece.setOnClickListener {
            when {
                proxy.getOwnPlayer().hasUsedTrade -> {
                    Toast.makeText(this, R.string.already_used_trade_move, Toast.LENGTH_SHORT)
                        .show()
                }
                gameLayout.isUsingTrade -> {
                    gameLayout.showPossibleMoves()
                    gameLayout.isUsingTrade = false
                }
                else -> {
                    clearPossibleMoves = gameLayout.clearPossibleMoves()
                    gameLayout.isUsingTrade = true
                }
            }
        }

        binding.btnPass.setOnClickListener{
            if (proxy.getPossibleMoves().isEmpty()){
                it.visibility = View.VISIBLE
                proxy.passPlayer()
            }else
                it.visibility = View.INVISIBLE
        }
    }

    private fun setCallbacks() {
        proxy.changePlayerCallback = l@{ id ->
            val player = proxy.getPlayerById(id)
            if (player == null) {
                Log.e(OURTAG, "Player is null from id : $id")
                Toast.makeText(this, "Player is null from id : $id", Toast.LENGTH_LONG).show()
                return@l
            }
            runOnUiThread {
                binding.tvPlayerName.text = player.profile.name
                binding.imgViewCurrentPlayer.background = player.profile.icon
                binding.imgViewCurrentPlayerPiece.background = player.piece.getDrawable(this)
                clearPossibleMoves = null
                // TODO 6 botão fazer toggle
                gameLayout.isUsingBombPiece = false
                gameLayout.isUsingTrade = false

                if (proxy.isOnline()) {
                    lastPlayerView?.view?.setBackgroundResource(R.drawable.piece_possible_black)
                    val currentPlayerView = playersView.find { it.playerId == id }!!
                    currentPlayerView.view.setBackgroundResource(R.drawable.piece_possible_white)
                    lastPlayerView = currentPlayerView
                }
            }
        }
        if (proxy.isOnline()) {
            val list = ArrayList<PlayerView>(3)
            proxy.getPlayers().forEach {
                val view = layoutInflater.inflate( // returns binding.layoutPlayers
                    R.layout.row_waiting_player, binding.layoutPlayers) as ViewGroup
                list.add(PlayerView(it.playerId, view[view.childCount - 1]))
            }
            playersView = list
        }

        proxy.gameFinishedCallback = {
            runOnUiThread {
                //Toast.makeText(this, "Game finished", Toast.LENGTH_SHORT).show()
                openEndGameLayoutDialog(it)
                val playerStats =
                    it.playerStats.find { p -> p.player.playerId == it.winningPlayerId }
                if (playerStats != null) {
                    Toast.makeText(this, "${playerStats.player.profile.name} " +
                            "-> ${playerStats.pieces} pieces", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Draw", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        proxy.detach()
    }

    //TODO SET INVISIBLE BUTTON
    //TODO winner pop up on end game

    private fun openEndGameLayoutDialog(gameEndStats: GameEndStats){
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.end_game_dialog_layout)
        val tvWinner = dialog.findViewById(R.id.tvWinner) as TextView
        val tvWinnerScore = dialog.findViewById(R.id.tvWinnerScore) as TextView
        val tvOpponent1 = dialog.findViewById(R.id.tvOpponent1) as TextView
        val tvScoreOpponent1 = dialog.findViewById(R.id.tvScoreOpponent1) as TextView
        val tvOpponent2 = dialog.findViewById(R.id.tvOpponent2) as TextView
        val tvScoreOpponent2 = dialog.findViewById(R.id.tvScoreOpponent2) as TextView
        val btnOk = dialog.findViewById(R.id.btnOk) as Button

        gameEndStats.playerStats.forEach {
            if (it.player.playerId == gameEndStats.winningPlayerId){
                tvWinner.text = it.player.profile.name
                tvWinnerScore.text = it.pieces.toString()
            }else{
                tvOpponent1.text = it.player.profile.name
                tvScoreOpponent1.text = it.pieces.toString()
                if (gameEndStats.playerStats.size > 2){
                    val flThirdPlayer = dialog.findViewById(R.id.lnThirdPlayer) as View
                    flThirdPlayer.visibility = View.VISIBLE
                    if (tvOpponent1.text != it.player.profile.name ){
                        tvOpponent2.text = it.player.profile.name
                        tvScoreOpponent2.text = it.pieces.toString()
                        //TODO test isto
                    }
                }
            }
        }
        btnOk.setOnClickListener{
            dialog.dismiss()
            finish()
        }

        dialog.show()
    }

    override fun onBackPressed() {
        val alertDialog = AlertDialog.Builder(this)
            .setTitle("Exit Game")
            .setMessage("Are you sure you want to leave the game?")
            .setPositiveButton(getString(R.string.yes)) { d, w -> finish() }
            .setNegativeButton(getString(R.string.no)) { dialog, w -> dialog.dismiss() }
            .setCancelable(true)
            .create()
        alertDialog.show()
    }

    /*override fun onSupportNavigateUp(): Boolean {
        val alertDialog = AlertDialog.Builder(this)
            .setTitle("Exit Game")
            .setMessage("Are you sure you want to leave the game?")
            .setPositiveButton(getString(R.string.yes)) { d, w -> finish() }
            .setNegativeButton(getString(R.string.no)) { dialog, w -> dialog.dismiss() }
            .setCancelable(true)
            .create()
        alertDialog.show()
        return true
    }*/
}