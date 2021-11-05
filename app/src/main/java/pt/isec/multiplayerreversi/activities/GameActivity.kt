package pt.isec.multiplayerreversi.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import pt.isec.multiplayerreversi.R

class GameActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
    }
}