package pt.isec.multiplayerreversi.activities

import android.os.Bundle
import android.util.DisplayMetrics
import android.widget.GridLayout
import androidx.appcompat.app.AppCompatActivity
import pt.isec.multiplayerreversi.App
import pt.isec.multiplayerreversi.R
import pt.isec.multiplayerreversi.game.GameGrid


class GameActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

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
    }
}