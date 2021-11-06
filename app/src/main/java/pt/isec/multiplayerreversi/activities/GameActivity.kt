package pt.isec.multiplayerreversi.activities

import android.os.Bundle
import android.util.DisplayMetrics
import android.widget.GridLayout
import androidx.appcompat.app.AppCompatActivity
import pt.isec.multiplayerreversi.R
import pt.isec.multiplayerreversi.game.GameGrid
import pt.isec.multiplayerreversi.game.interactors.senders.InteractionSenderProxy
import pt.isec.multiplayerreversi.game.logic.Game
import pt.isec.multiplayerreversi.game.logic.Player


class GameActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        val gridLayout = findViewById<GridLayout>(R.id.gridContainer)
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)

        val boardSideLength = intent.getIntExtra("boardSideLength", -1)
        if (boardSideLength == -1)
            throw IllegalStateException("Board side length must come in the intent extras")

        val proxy =
            intent.getSerializableExtra("interactionSenderProxy") as InteractionSenderProxy
        val players = intent.getSerializableExtra("players") as ArrayList<Player>

        val gameLayout =
            GameGrid(this, gridLayout, displayMetrics, layoutInflater, boardSideLength, proxy)
        val game = Game(boardSideLength, players, players.random())
        gameLayout.updatePieces(game.getBoard())
    }
}