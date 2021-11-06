package pt.isec.multiplayerreversi.activities

import android.os.Bundle
import android.util.DisplayMetrics
import android.widget.GridLayout
import androidx.appcompat.app.AppCompatActivity
import pt.isec.multiplayerreversi.R
import pt.isec.multiplayerreversi.game.GameGrid


class GameActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        val gridLayout = findViewById<GridLayout>(R.id.gridContainer)

        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)

        val gameLayout = GameGrid(this, gridLayout, displayMetrics, layoutInflater, 10)
    }
}