package pt.isec.multiplayerreversi.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import pt.isec.multiplayerreversi.App
import pt.isec.multiplayerreversi.R
import pt.isec.multiplayerreversi.databinding.ActivityLaucherBinding
import pt.isec.multiplayerreversi.game.interactors.local.Local1V1Interaction
import pt.isec.multiplayerreversi.game.logic.Game
import pt.isec.multiplayerreversi.game.logic.Piece
import pt.isec.multiplayerreversi.game.logic.Player
import pt.isec.multiplayerreversi.game.logic.Profile

class LauncherActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLaucherBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLaucherBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.avatarIcon.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            startActivity(intent)
        }

        binding.btnLocal.setOnClickListener {
            val intent = Intent(this, GameActivity::class.java)

            val players = ArrayList<Player>(2)
            players.add(Player(Profile(resources.getString(R.string.dark_piece)), Piece.Dark))
            players.add(Player(Profile(resources.getString(R.string.light_piece)), Piece.Light))

            val app = application as App
            val game = Game(8, players, players.random())
            app.game = game
            app.interaction = Local1V1Interaction(game)
            startActivity(intent)
        }

        binding.btnRemote.setOnClickListener {
            val intent = Intent(this, WaitingAreaActivity::class.java)
            intent.putExtra("playerAmount", 2)
            startActivity(intent)
        }

        //TODO 100 mudar drawable para icon do utilizador
        //TODO 10000 fazer logo com a pixa
    }

}