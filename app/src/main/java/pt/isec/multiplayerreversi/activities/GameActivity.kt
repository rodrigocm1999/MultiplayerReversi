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

class GameActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGameBinding

    private lateinit var playersView: List<PlayerView>
    private var lastPlayerView: PlayerView? = null

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
            if (gameLayout.isUsingTrade) return@setOnClickListener
            when {
                !gamePlayer.getOwnPlayer().canUseBomb() -> {
                    Toast.makeText(this, R.string.already_used_bomb_piece, Toast.LENGTH_SHORT)
                        .show()
                }
                gameLayout.isUsingBombPiece -> {
                    gameLayout.showPossibleMoves()
                    gameLayout.isUsingBombPiece = false
                    it.background = null
                }
                else -> {
                    gameLayout.clearPossibleMoves()
                    gameLayout.isUsingBombPiece = true
                    it.setBackgroundResource(R.color.clickedSpecial)
                }
            }
        }

        binding.btnTradePiece.setOnClickListener {
            if (gameLayout.isUsingBombPiece) return@setOnClickListener
            when {
                !gamePlayer.getOwnPlayer().canUseTrade() -> {
                    Toast.makeText(this, R.string.already_used_trade_move, Toast.LENGTH_SHORT)
                        .show()
                }
                gameLayout.isUsingTrade -> {
                    gameLayout.showPossibleMoves()
                    gameLayout.isUsingTrade = false
                    it.background = null
                }
                else -> {
                    gameLayout.clearPossibleMoves()
                    gameLayout.isUsingTrade = true
                    it.setBackgroundResource(R.color.clickedSpecial)
                }
            }
        }

        binding.btnPass.setOnClickListener {
            gamePlayer.passPlayer()
        }
    }

    private fun setCallbacks() {
        gamePlayer.updateBoardCallback = {
            runOnUiThread {
                gameLayout.updatePieces()
            }
        }
        gamePlayer.possibleMovesCallback = { possibleMoves ->
            runOnUiThread {
                if (possibleMoves.isNotEmpty()) {
                    gameLayout.showPossibleMoves()
                    binding.btnPass.visibility = View.GONE
                } else {
                    binding.btnPass.visibility = View.VISIBLE
                }
            }
        }

        gamePlayer.playerUsedBombCallback = { id ->
            if (gamePlayer.isOnline()) {
                val playerView = playersView.find { it.playerId == id }
                runOnUiThread {
                    playerView?.ivBomb?.visibility = View.GONE
                }
            }
        }

        gamePlayer.playerUsedTradeCallback = { id ->
            if (gamePlayer.isOnline()) {
                val playerView = playersView.find { it.playerId == id }
                runOnUiThread {
                    playerView?.ivTrade?.visibility = View.GONE
                }
            }
        }

        gamePlayer.changePlayerCallback = { id ->
            runOnUiThread {
                val player = gamePlayer.getPlayerById(id)
                if (player == null) {
                    Log.e(OURTAG, "Player is null from id : $id")
                    Toast.makeText(this, "Player is null from id : $id", Toast.LENGTH_LONG).show()
                    return@runOnUiThread
                }
                val isPlayerTurn = id == gamePlayer.getOwnPlayer().playerId
                if (isPlayerTurn)
                    updatePlayerButtons()
                else {
                    binding.btnBombPiece.visibility = View.GONE
                    binding.btnTradePiece.visibility = View.GONE
                    binding.btnPass.visibility = View.GONE
                }

                binding.btnTradePiece.background = null
                binding.btnBombPiece.background = null

                binding.tvPlayerName.text = player.profile.name
                binding.imgViewCurrentPlayer.setImageDrawable(player.profile.getIcon(this))
                binding.imgViewCurrentPlayerPiece.setImageDrawable(
                    player.piece.getIsolatedDrawable(this))
                gameLayout.isUsingBombPiece = false
                gameLayout.isUsingTrade = false

                if (gamePlayer.isOnline()) {
                    lastPlayerView?.parentView?.background = null
                    val currentPlayerView = playersView.find { it.playerId == id }!!
                    currentPlayerView.parentView.setBackgroundResource(R.color.clickedSpecial)
                    lastPlayerView = currentPlayerView
                }
            }
        }
        if (gamePlayer.isOnline()) {
            val list = ArrayList<PlayerView>(3)
            gamePlayer.getPlayers().forEach { player ->
                val linearLayout = layoutInflater.inflate( // returns binding.layoutPlayers
                    R.layout.playing_player, binding.layoutPlayers) as ViewGroup
                val playerParentView = linearLayout[linearLayout.childCount - 1]
                val playerView = PlayerView(player.playerId, playerParentView,
                    ivPlayerImg = playerParentView.findViewById(R.id.imgViewPlayerIcon),
                    tvPlayerName = playerParentView.findViewById(R.id.textViewPlayerName),
                    ivPlayerPiece = playerParentView.findViewById(R.id.imgViewPlayerPiece),
                    ivBomb = playerParentView.findViewById(R.id.imgViewBombState),
                    ivTrade = playerParentView.findViewById(R.id.imgViewTradeState))
                playerView.ivPlayerImg.setImageDrawable(player.profile.icon)
                playerView.tvPlayerName.text = player.profile.name
                playerView.ivPlayerPiece.setImageDrawable(player.piece.getIsolatedDrawable(this))
                list.add(playerView)
            }
            playersView = list
        }

        gamePlayer.gameFinishedCallback = {
            runOnUiThread {
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

    private fun updatePlayerButtons() {
        val currentPlayer = gamePlayer.getCurrentPlayer()
        if (currentPlayer.canUseBomb()) {
            binding.btnBombPiece.visibility = View.VISIBLE
            binding.imgViewCurrentBombState.visibility = View.VISIBLE
        } else {
            binding.btnBombPiece.visibility = View.GONE
            binding.imgViewCurrentBombState.visibility = View.GONE
        }
        if (currentPlayer.canUseTrade()) {
            binding.btnTradePiece.visibility = View.VISIBLE
            binding.imgViewCurrentTradeState.visibility = View.VISIBLE
        } else {
            binding.btnTradePiece.visibility = View.GONE
            binding.imgViewCurrentTradeState.visibility = View.GONE
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


        if (gameEndStats.winningPlayerId == -1) {
            val tvGameResult = dialog.findViewById(R.id.tvGameResult) as TextView
            tvGameResult.text = getString(R.string.draw)
            tvWinner.text = gamePlayer.getOwnPlayer().profile.name
        }

        gameEndStats.playerStats.forEach {
            if (it.player.playerId == gameEndStats.winningPlayerId) {
                tvWinner.text = it.player.profile.name
                tvWinnerScore.text = it.pieces.toString()
            } else if (tvWinner.text != it.player.profile.name) {
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

    data class PlayerView(
        val playerId: Int,
        val parentView: View,
        val ivPlayerImg: ImageView,
        val tvPlayerName: TextView,
        val ivPlayerPiece: ImageView,
        val ivBomb: ImageView,
        val ivTrade: ImageView,
    )

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