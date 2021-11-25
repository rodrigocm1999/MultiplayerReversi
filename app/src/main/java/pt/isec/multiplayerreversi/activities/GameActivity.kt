package pt.isec.multiplayerreversi.activities

import android.graphics.Color
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import pt.isec.multiplayerreversi.App
import pt.isec.multiplayerreversi.App.Companion.OURTAG
import pt.isec.multiplayerreversi.R
import pt.isec.multiplayerreversi.databinding.ActivityGameBinding
import pt.isec.multiplayerreversi.game.GameGrid
import pt.isec.multiplayerreversi.game.interactors.GamePlayer
import pt.isec.multiplayerreversi.game.logic.Vector

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
    }

    private fun setCallbacks() {
        proxy.changePlayerCallback = l@{ id ->
            val player = proxy.getPlayerById(id)
            if (player == null) {
                Log.e(OURTAG, "Player is null from id : $id")
                Toast.makeText(this, "Player is null from id : $id", Toast.LENGTH_LONG).show()
                return@l
            }
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
            Toast.makeText(this, "Game finished", Toast.LENGTH_SHORT).show()
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

    override fun onDestroy() {
        super.onDestroy()
        proxy.detach()
    }
}