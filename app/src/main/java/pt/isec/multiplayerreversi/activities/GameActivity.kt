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
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import pt.isec.multiplayerreversi.App
import pt.isec.multiplayerreversi.App.Companion.OURTAG
import pt.isec.multiplayerreversi.R
import pt.isec.multiplayerreversi.activities.others.FirestoreHelper
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
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        db = Firebase.firestore

        app = application as App
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

        playersView?.forEach { playerView ->
            if (!playerView.player.canUseBomb()) playerView.ivBomb.visibility = View.INVISIBLE
            if (!playerView.player.canUseTrade()) playerView.ivTrade.visibility = View.INVISIBLE
            playerView.tvPlayerScore.text = playerView.player.score.toString()
        }
        val player = gamePlayer.getOwnPlayer()
        updateCurrentPlayerTopBar(player)
        if (gameLayout.gameEnded) {
            finish()
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
        gamePlayer.updateBoardCallback = {
            runOnUiThread {
                gameLayout.updatePieces()
                playersView?.forEach {
                    it.tvPlayerScore.text = it.player.score.toString()
                }
            }
        }

        gamePlayer.possibleMovesCallback = { possibleMoves ->
            if (gamePlayer.getCurrentPlayer() == gamePlayer.getOwnPlayer()) {
                runOnUiThread {
                    gameLayout.showPossibleMoves()
                    binding.btnPass.visibility =
                        if (possibleMoves.isEmpty()) View.VISIBLE else View.GONE

//                    if (!gamePlayer.playerHasAnyMoves()) {
//                        gamePlayer.passPlayer()
//                        binding.btnPass.visibility = View.GONE
//                        Toast.makeText(this, R.string.you_had_no_possible_moves, Toast.LENGTH_LONG)
//                            .show()
//                    }
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
                updateCurrentPlayerTopBar(player)

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
                    ivTrade = parentView.findViewById(R.id.imgViewTradeState),
                    tvPlayerScore = parentView.findViewById(R.id.tvPlayerScore))
                playerView.ivPlayerImg.setImageDrawable(player.profile.icon)
                playerView.tvPlayerName.text = player.profile.name
                playerView.tvPlayerScore.text = player.score.toString()
                playerView.ivPlayerPiece.setImageDrawable(player.piece.getIsolatedDrawable(this))
                list.add(playerView)
            }
            playersView = list
        }

        gamePlayer.gameFinishedCallback = {
            if (gamePlayer.isOnline() && it.winningPlayerId == gamePlayer.getOwnPlayer().playerId)
                saveScoreIfThatIsThyWish(it)
            gameLayout.gameEnded = true
            runOnUiThread { openEndGameLayoutDialog(it) }

        }
        gamePlayer.gameTerminatedCallback = { runOnUiThread { gameTerminated() } }
    }

    private fun updateCurrentPlayerTopBar(player: Player) {
        binding.tvPlayerName.text = player.profile.name
        binding.imgViewCurrentPlayer.setImageDrawable(player.profile.getIcon(this))
        binding.imgViewCurrentPlayerPiece.setImageDrawable(
            player.piece.getIsolatedDrawable(this))
        binding.imgViewCurrentBombState.visibility =
            if (player.canUseBomb()) View.VISIBLE else View.INVISIBLE
        binding.imgViewCurrentTradeState.visibility =
            if (player.canUseTrade()) View.VISIBLE else View.INVISIBLE
        binding.tvPlayerNowPlaying.text = player.score.toString()
    }

    private fun saveScoreIfThatIsThyWish(gameEndStats: GameEndStats) {
        thread {
            val ownPlayerStatus = gameEndStats.playerStats.find { playerEndStats ->
                gamePlayer.getOwnPlayer().playerId == playerEndStats.player.playerId
            }!!
            val helper = FirestoreHelper(ownPlayerStatus.player.profile.email!!)
            helper.insertData(gameEndStats, ownPlayerStatus.pieces)
        }
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
        val llEndPlayers = dialog.findViewById(R.id.llEndPlayers) as LinearLayout
        val btnOk = dialog.findViewById(R.id.btnOk) as Button


        if (gameEndStats.winningPlayerId == -1) {
            val tvGameResult = dialog.findViewById(R.id.tvGameResult) as TextView
            tvGameResult.text = getString(R.string.draw)
            tvWinner.text = gamePlayer.getOwnPlayer().profile.name
        }
        val sortedByDescending = gameEndStats.playerStats.sortedByDescending { it.pieces }
        val list = ArrayList<EndPlayerView>(3)

        sortedByDescending.forEach {
            if (it.player.playerId == gameEndStats.winningPlayerId) {
                tvWinner.text = it.player.profile.name
            }
            val linearLayout = layoutInflater.inflate( // returns binding.layoutPlayers
                R.layout.end_game_player, llEndPlayers
            ) as ViewGroup
            val parentView = linearLayout[linearLayout.childCount - 1]
            val endPlayerView = EndPlayerView(
                mvEndPlayerIcon = parentView.findViewById(R.id.mvEndPlayerIcon),
                tvEndPlayerName = parentView.findViewById(R.id.tvEndPlayerName),
                tvPlayerScore = parentView.findViewById(R.id.tvPlayerScore)
            )
            if (!gamePlayer.isOnline()) endPlayerView.mvEndPlayerIcon.visibility = View.GONE

            endPlayerView.mvEndPlayerIcon.setImageDrawable(it.player.profile.icon)
            endPlayerView.tvEndPlayerName.text = it.player.profile.name
            endPlayerView.tvPlayerScore.text = it.pieces.toString()
            list.add(endPlayerView)

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
            .setPositiveButton(getString(R.string.yes)) { _, _ -> startGameLocallyFromCurrentOnlineGame() }
            .setNegativeButton(getString(R.string.no)) { _, _ -> finish() }
            .setCancelable(false)
            .create()
        alertDialog.show()
    }

    private fun startGameLocallyFromCurrentOnlineGame() {
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

    override fun onDestroy() {
        super.onDestroy()
        app.gamePlayer?.leaveGame() // TODO verificar se isto causa algum problema
    }


    private fun leaveGame() {
        gamePlayer.leaveGame()
        unreferenceGame()
        finish()
    }

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
        val tvPlayerScore: TextView,
    )

    data class EndPlayerView(
        val mvEndPlayerIcon: ImageView,
        val tvEndPlayerName: TextView,
        val tvPlayerScore: TextView,
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