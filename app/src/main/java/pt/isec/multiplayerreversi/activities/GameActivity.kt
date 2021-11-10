package pt.isec.multiplayerreversi.activities

import android.os.Bundle
import android.util.DisplayMetrics
import android.widget.GridLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import pt.isec.multiplayerreversi.App
import pt.isec.multiplayerreversi.R
import pt.isec.multiplayerreversi.databinding.ActivityGameBinding
import pt.isec.multiplayerreversi.game.GameGrid


class GameActivity : AppCompatActivity() {

    lateinit var binding: ActivityGameBinding;

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

        game.start()

        binding.btnBombPiece.setOnClickListener{
            if (game.getCurrentPlayer().hasUsedBomb()){
                Toast.makeText(this,"You have already use the bomb",Toast.LENGTH_SHORT).show()
            }else{
                gameLayout.isPlayingBombPiece = true
            }
        }
        binding.tvPlayerName.text = game.getCurrentPlayer().getProfile().name
    }
}