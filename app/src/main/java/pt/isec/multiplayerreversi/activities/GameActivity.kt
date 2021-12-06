package pt.isec.multiplayerreversi.activities

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.ImageView
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

class GameActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGameBinding

    private lateinit var playersView: List<PlayerView>
    private var lastPlayerView: PlayerView? = null

    private var clearPossibleMoves: List<Vector>? = null
    private lateinit var gamePlayer: GamePlayer
    private lateinit var gameLayout: GameGrid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)

        val app = application as App
        gamePlayer = app.gamePlayer
            ?: throw IllegalStateException("InteractionSender from App is null when entering the game activity")

        gameLayout = GameGrid(this, binding.gridContainer, displayMetrics,
            layoutInflater, gamePlayer.getGameSideLength(), gamePlayer)

        setCallbacks()
        setListeners()

        gamePlayer.ready()
    }

    override fun onStart() {
        super.onStart()
        gameLayout.refreshBoard()
    }

    private fun setListeners() {
        binding.btnBombPiece.setOnClickListener {
            when {
                gamePlayer.getOwnPlayer().hasUsedBomb -> {
                    Toast.makeText(this, R.string.already_used_bomb_piece, Toast.LENGTH_SHORT)
                        .show()
                }
                gameLayout.isUsingBombPiece -> {
                    gameLayout.showPossibleMoves()
                    gameLayout.isUsingBombPiece = false
                }
                else -> {
                    gameLayout.clearPossibleMoves()
                    gameLayout.isUsingBombPiece = true
                }
            }
        }

        binding.btnTradePiece.setOnClickListener {
            when {
                gamePlayer.getOwnPlayer().hasUsedTrade -> {
                    Toast.makeText(this, R.string.already_used_trade_move, Toast.LENGTH_SHORT)
                        .show()
                }
                gameLayout.isUsingTrade -> {
                    gameLayout.showPossibleMoves()
                    gameLayout.isUsingTrade = false
                }
                else -> {
                    gameLayout.clearPossibleMoves()
                    gameLayout.isUsingTrade = true
                }
            }
        }

        binding.btnPass.setOnClickListener {
            gamePlayer.passPlayer()
        }
    }

    private fun setCallbacks() {
        gamePlayer.updateBoardCallback = { changedBoard ->
            runOnUiThread {
                gameLayout.updatePieces()
            }
        }
        gamePlayer.possibleMovesCallback = { possibleMoves ->
            runOnUiThread {
                gameLayout.showPossibleMoves()
                binding.btnPass.visibility =
                    if (possibleMoves.isEmpty()) View.VISIBLE else View.INVISIBLE
            }
        }

        gamePlayer.changePlayerCallback = l@{ id ->
            val player = gamePlayer.getPlayerById(id)
            if (player == null) {
                Log.e(OURTAG, "Player is null from id : $id")
                Toast.makeText(this, "Player is null from id : $id", Toast.LENGTH_LONG).show()
                return@l
            }
            val shouldShowButtons = id == gamePlayer.getCurrentPlayer().playerId
            runOnUiThread {
                if (shouldShowButtons)
                    updatePlayerButtons()


                //TODO acabar de meter as cenas no topo do ecra meter os icones bem entre outras cenas


                binding.tvPlayerName.text = player.profile.name
                binding.imgViewCurrentPlayer.background = player.profile.icon
                binding.imgViewCurrentPlayerPiece.background = player.piece.getDrawable(this)
                clearPossibleMoves = null
                gameLayout.isUsingBombPiece = false
                gameLayout.isUsingTrade = false

                if (gamePlayer.isOnline()) {
                    lastPlayerView?.parentView?.setBackgroundResource(R.drawable.piece_possible_black)
                    val currentPlayerView = playersView.find { it.playerId == id }!!
                    currentPlayerView.parentView.setBackgroundResource(R.drawable.piece_possible_white)
                    lastPlayerView = currentPlayerView
                }
            }
        }
        if (gamePlayer.isOnline()) {
            val list = ArrayList<PlayerView>(3)
            gamePlayer.getPlayers().forEach { player ->
                val view = layoutInflater.inflate( // returns binding.layoutPlayers
                    R.layout.playing_player, binding.layoutPlayers) as ViewGroup
                val parentView = view[view.childCount - 1]
                val playerView = PlayerView(player.playerId, parentView,
                    ivPlayerImg = view.findViewById(R.id.imgViewPlayerIcon),
                    tvPlayerName = view.findViewById(R.id.tvPlayerName),
                    ivPlayerPiece = view.findViewById(R.id.imgViewPlayerPiece),
                    ivBomb = view.findViewById(R.id.imgViewBombState),
                    ivTrade = view.findViewById(R.id.imgViewTradeState)
                )
                playerView.ivPlayerImg.setImageDrawable(player.profile.icon)
                playerView.tvPlayerName.text = player.profile.name
                playerView.ivPlayerPiece.setImageDrawable(player.piece.getIsolatedDrawable(this))
                //TODO mudar os estados quando um player joga uma peça especial
                list.add(playerView)
            }
            playersView = list
        }

        gamePlayer.gameFinishedCallback = {
            runOnUiThread {
                //Toast.makeText(this, "Game finished", Toast.LENGTH_SHORT).show()
                openEndGameLayoutDialog(it)
                /*val playerStats =
                    it.playerStats.find { p -> p.player.playerId == it.winningPlayerId }
                if (playerStats != null) {
                    Toast.makeText(this, "${playerStats.player.profile.name} " +
                            "-> ${playerStats.pieces} pieces", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Draw", Toast.LENGTH_SHORT).show()
                }*/
            }
        }
    }

    private fun updatePlayerButtons(){

        val currentPlayer = gamePlayer.getCurrentPlayer()
        if (currentPlayer.hasUsedTrade) {
            binding.btnTradePiece.visibility = View.GONE
            binding.imgViewCurrentTradeState.visibility = View.GONE
        }
        else {
            binding.btnTradePiece.visibility = View.VISIBLE
            binding.imgViewCurrentTradeState.visibility = View.VISIBLE
        }
        if (currentPlayer.hasUsedBomb){
            binding.btnBombPiece.visibility = View.GONE
            binding.imgViewCurrentBombState.visibility = View.GONE
        }else{
            binding.btnBombPiece.visibility = View.VISIBLE
            binding.imgViewCurrentBombState.visibility = View.VISIBLE
        }
    }

    private fun openEndGameLayoutDialog(gameEndStats: GameEndStats) {
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


        if (gameEndStats.winningPlayerId == -1){
            val tvGameResult = dialog.findViewById(R.id.tvGameResult) as TextView
            tvGameResult.text = getString(R.string.draw)
            tvWinner.text = gamePlayer.getOwnPlayer().profile.name
        }

        gameEndStats.playerStats.forEach {
            if (it.player.playerId == gameEndStats.winningPlayerId) {
                tvWinner.text = it.player.profile.name
                tvWinnerScore.text = it.pieces.toString()
            } else if(tvWinner.text != it.player.profile.name) {
                tvOpponent1.text = it.player.profile.name
                tvScoreOpponent1.text = it.pieces.toString()
                if (gameEndStats.playerStats.size > 2) {
                    val flThirdPlayer = dialog.findViewById(R.id.lnThirdPlayer) as View
                    flThirdPlayer.visibility = View.VISIBLE

                    if (tvOpponent1.text != it.player.profile.name) {
                        tvOpponent2.text = it.player.profile.name
                        tvScoreOpponent2.text = it.pieces.toString()
                        //TODO testar janela dos scores com 3 pessoas
                    }
                }
            }
        }
        btnOk.setOnClickListener {
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

    class PlayerView(
        val playerId: Int,
        val parentView: View,
        val ivPlayerImg: ImageView,
        val tvPlayerName: TextView,
        val ivPlayerPiece: ImageView,
        val ivBomb: ImageView,
        val ivTrade: ImageView,
    ) {
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