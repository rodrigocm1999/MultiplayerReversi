package pt.isec.multiplayerreversi.activities

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
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
import pt.isec.multiplayerreversi.game.interactors.LocalPlayer
import pt.isec.multiplayerreversi.game.logic.Game
import pt.isec.multiplayerreversi.game.logic.GameEndStats
import pt.isec.multiplayerreversi.game.logic.Player
import kotlin.concurrent.thread

class GameActivity : AppCompatActivity() {

    private lateinit var app: App
    private lateinit var binding: ActivityGameBinding

    private var playersView: List<PlayerView>? = null
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

        app = application as App
        gamePlayer = app.gamePlayer
            ?: throw IllegalStateException("InteractionSender from App is null when entering the game activity")

        gameLayout = GameGrid(this, binding.gridContainer, displayMetrics,
            layoutInflater, gamePlayer.getGameSideLength(), gamePlayer)

        setCallbacks()
        setListeners()
        //TODO scores no layout , se calhar tanto em cima como em baixo
        gamePlayer.ready()
    }


    override fun onStart() {
        super.onStart()
        gameLayout.refreshBoard()
        playersView?.forEach { playerView ->
            if (!playerView.player.canUseBomb()) playerView.ivBomb.visibility = View.INVISIBLE
            if (!playerView.player.canUseTrade()) playerView.ivTrade.visibility = View.INVISIBLE
        }
        val player = gamePlayer.getOwnPlayer()
        if (player.playerId == gamePlayer.getCurrentPlayer().playerId) {
            binding.btnPass.visibility =
                if (gamePlayer.getPossibleMoves().isEmpty()) View.VISIBLE else View.GONE
            binding.btnBombPiece.visibility = if (player.canUseBomb()) View.VISIBLE else View.GONE
            binding.btnTradePiece.visibility = if (player.canUseTrade()) View.VISIBLE else View.GONE
        }
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
        gamePlayer.updateBoardCallback = { runOnUiThread { gameLayout.updatePieces() } }

        gamePlayer.possibleMovesCallback = { possibleMoves ->
            if (gamePlayer.getCurrentPlayer() == gamePlayer.getOwnPlayer()) {
                runOnUiThread {
                    gameLayout.showPossibleMoves()
                    binding.btnPass.visibility =
                        if (possibleMoves.isEmpty()) View.VISIBLE else View.GONE

                    if (!gamePlayer.playerHasAnyMoves()) {
                        gamePlayer.passPlayer()
                        binding.btnPass.visibility = View.GONE
                        Toast.makeText(this, R.string.you_had_no_possible_moves, Toast.LENGTH_LONG)
                            .show()
                    }
                }
            }
        }

        gamePlayer.playerUsedBombCallback = { id ->
            if (gamePlayer.isOnline()) {
                val playerView = playersView?.find { it.player.playerId == id }
                playerView?.let { runOnUiThread { it.ivBomb.visibility = View.GONE } }
            }
        }

        gamePlayer.playerUsedTradeCallback = { id ->
            if (gamePlayer.isOnline()) {
                val playerView = playersView?.find { it.player.playerId == id }
                playerView?.let { runOnUiThread { it.ivTrade.visibility = View.GONE } }
            }
        }

        gamePlayer.changePlayerCallback = { id ->
            runOnUiThread {
                val player = gamePlayer.getPlayerById(id)
                if (player == null) {
                    Log.e(OURTAG, "Player is null from id : $id")
                    Toast.makeText(this, "Player is null from id : $id", Toast.LENGTH_LONG)
                        .show()
                    return@runOnUiThread
                }
                val isPlayerTurn = id == gamePlayer.getOwnPlayer().playerId
                if (isPlayerTurn)
                    updatePlayerButtons(player)
                else {
                    binding.btnBombPiece.visibility = View.GONE
                    binding.btnTradePiece.visibility = View.GONE
                    binding.btnPass.visibility = View.GONE
                }

                binding.btnBombPiece.background = null
                binding.btnTradePiece.background = null
                gameLayout.isUsingBombPiece = false
                gameLayout.isUsingTrade = false
                updateCurrentPlayerBar(player)

                if (gamePlayer.isOnline()) {
                    lastPlayerView?.parentView?.background = null
                    val currentPlayerView = playersView?.find { it.player == player }
                    currentPlayerView?.parentView?.setBackgroundResource(R.color.clickedSpecial)
                    lastPlayerView = currentPlayerView
                }
            }
        }

        if (gamePlayer.isOnline()) {
//        if (true) {
            val list = ArrayList<PlayerView>(3)
            gamePlayer.getPlayers().forEach { player ->
                val linearLayout = layoutInflater.inflate( // returns binding.layoutPlayers
                    R.layout.playing_player, binding.layoutPlayers) as ViewGroup
                val parentView = linearLayout[linearLayout.childCount - 1]
                val playerView = PlayerView(player, parentView,
                    ivPlayerImg = parentView.findViewById(R.id.imgViewPlayerIcon),
                    tvPlayerName = parentView.findViewById(R.id.textViewPlayerName),
                    ivPlayerPiece = parentView.findViewById(R.id.imgViewPlayerPiece),
                    ivBomb = parentView.findViewById(R.id.imgViewBombState),
                    ivTrade = parentView.findViewById(R.id.imgViewTradeState))
                playerView.ivPlayerImg.setImageDrawable(player.profile.icon)
                playerView.tvPlayerName.text = player.profile.name
                playerView.ivPlayerPiece.setImageDrawable(player.piece.getIsolatedDrawable(this))
                list.add(playerView)
            }
            playersView = list
        }

        gamePlayer.gameFinishedCallback = {
            if (gamePlayer.isOnline() && app.game != null) {
                thread { saveScoreIfThatIsThyWish() }
            }
            runOnUiThread { openEndGameLayoutDialog(it) }
        }
        gamePlayer.gameTerminatedCallback = { runOnUiThread { gameTerminated() } }
    }

    private fun updateCurrentPlayerBar(player: Player) {
        binding.tvPlayerName.text = player.profile.name
        binding.imgViewCurrentPlayer.setImageDrawable(player.profile.getIcon(this))
        binding.imgViewCurrentPlayerPiece.setImageDrawable(
            player.piece.getIsolatedDrawable(this))
        binding.imgViewCurrentBombState.visibility =
            if (player.canUseBomb()) View.VISIBLE else View.INVISIBLE
        binding.imgViewCurrentTradeState.visibility =
            if (player.canUseTrade()) View.VISIBLE else View.INVISIBLE
    }

    private fun saveScoreIfThatIsThyWish() {
        //TODO save the game on the table
    }

    private fun updatePlayerButtons(player: Player) {
        binding.btnBombPiece.visibility =
            if (player.canUseBomb()) View.VISIBLE else View.GONE
        binding.btnTradePiece.visibility =
            if (player.canUseTrade()) View.VISIBLE else View.GONE
        binding.btnPass.visibility =
            if (gamePlayer.getPossibleMoves().isEmpty()) View.VISIBLE else View.GONE
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
            unreferenceGame()
            finish()
        }

        dialog.show()
    }

    private fun gameTerminated() {
        val alertDialog = AlertDialog.Builder(this)
            .setTitle(R.string.game_interrupted)
            .setMessage(R.string.ask_keep_locally)
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                val game = Game(gamePlayer.getGameData())
                val gamePlayer = LocalPlayer(game)
                game.players.forEach { it.callbacks = null }
                game.players[0].callbacks = gamePlayer
                app.game = game
                app.gamePlayer = gamePlayer
                val intent = Intent(this, GameActivity::class.java)
                startActivity(intent)
                finish()
            }
            .setNegativeButton(getString(R.string.no)) { _, _ ->
                finish()
            }
            .setCancelable(false)
            .create()
        alertDialog.show()
    }

    private fun leaveGame() {
        gamePlayer.leaveGame()
        unreferenceGame()
        finish()
    }

    //TODO resolver o problema do tabuleiro usar muito espaço em ecrãs mais quadrados

    private fun unreferenceGame() {
        app.gamePlayer = null
        app.game = null
    }

    override fun onBackPressed() {
        val alertDialog = AlertDialog.Builder(this)
            .setTitle(R.string.exit_game)
            .setMessage(R.string.ask_leave_game)
            .setPositiveButton(getString(R.string.yes)) { _, _ -> leaveGame() }
            .setNegativeButton(getString(R.string.no)) { dialog, _ -> dialog.dismiss() }
            .setCancelable(true)
            .create()
        alertDialog.show()
    }

    data class PlayerView(
        val player: Player,
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