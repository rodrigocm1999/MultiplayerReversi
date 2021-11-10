package pt.isec.multiplayerreversi.activities

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.widget.GridLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import pt.isec.multiplayerreversi.App
import pt.isec.multiplayerreversi.OURTAG
import pt.isec.multiplayerreversi.R
import pt.isec.multiplayerreversi.databinding.ActivityGameBinding
import pt.isec.multiplayerreversi.game.GameGrid
import pt.isec.multiplayerreversi.game.logic.Piece
import pt.isec.multiplayerreversi.game.logic.Vector

class GameActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGameBinding
    private var clearPossibleMoves: List<Vector>? = null

    private var darkPiece: Drawable? = null
    private var lightPiece: Drawable? = null
    private var bluePiece: Drawable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameBinding.inflate(layoutInflater);
        setContentView(binding.root)

        val gridLayout = findViewById<GridLayout>(R.id.gridContainer)
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)

        val app = application as App
        val game = app.game
            ?: throw IllegalStateException("Game from App is null when entering the game activity")
        val localInteractionSender = app.interaction
            ?: throw IllegalStateException("InteractionSender from App is null when entering the game activity")

        val gameLayout = GameGrid(
            this, gridLayout, displayMetrics,
            layoutInflater, game.getSideLength(), localInteractionSender
        )

        darkPiece = AppCompatResources.getDrawable(this, R.drawable.piece_dark)
        lightPiece = AppCompatResources.getDrawable(this, R.drawable.piece_light)
        bluePiece = AppCompatResources.getDrawable(this, R.drawable.piece_blue)

        localInteractionSender.setChangePlayerCallback {
            val player = localInteractionSender.getPlayerById(it)
            if (player == null) {
                Log.i(OURTAG, "Player is null from id : $it")
                return@setChangePlayerCallback
            }
            binding.tvPlayerName.text = player.getProfile().name
//            binding.imgViewCurrentPlayer.background =
            binding.imgViewCurrentPlayerPiece.background = when (player.getPiece()) {
                Piece.Dark -> darkPiece
                Piece.Light -> lightPiece
                Piece.Blue -> bluePiece
                else -> darkPiece
            }
            clearPossibleMoves = null

            // 6 TODO botão fazer toggle

            gameLayout.isUsingBombPiece = false
            gameLayout.isUsingTrade = false
        }

        binding.btnBombPiece.setOnClickListener {
            when {
                game.getCurrentPlayer().hasUsedBomb() -> {
                    Toast.makeText(this, "You have already use the bomb", Toast.LENGTH_SHORT).show()
                }
                gameLayout.isUsingBombPiece -> {
                    gameLayout.showPossibleMoves(clearPossibleMoves)
                    gameLayout.isUsingBombPiece = false
                }
                else -> {
                    clearPossibleMoves = gameLayout.clearPossibleMoves()
                    gameLayout.isUsingBombPiece = true
                }
            }
        }

        game.start()
    }
}