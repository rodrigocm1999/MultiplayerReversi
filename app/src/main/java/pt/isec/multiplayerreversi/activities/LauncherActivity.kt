package pt.isec.multiplayerreversi.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import pt.isec.multiplayerreversi.App
import pt.isec.multiplayerreversi.R
import pt.isec.multiplayerreversi.databinding.ActivityLaucherBinding
import pt.isec.multiplayerreversi.game.interactors.Local1V1Play
import pt.isec.multiplayerreversi.game.logic.Game
import pt.isec.multiplayerreversi.game.logic.Piece
import pt.isec.multiplayerreversi.game.logic.Player
import pt.isec.multiplayerreversi.game.logic.Profile

class LauncherActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLaucherBinding
    private lateinit var app: App

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLaucherBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        app = application as App
        setListeners()
    }

    private fun setListeners() {
        binding.avatarIcon.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            startActivity(intent)
        }
        binding.btnLocal.setOnClickListener {
            val intent = Intent(this, GameActivity::class.java)

            val players = ArrayList<Player>(2)
            players.add(Player(Profile(resources.getString(R.string.dark_piece)), Piece.Dark))
            players.add(Player(Profile(resources.getString(R.string.light_piece)), Piece.Light))

            val game = Game(8, players, players.random())
            app.game = game
            app.proxy = Local1V1Play(game)
            startActivity(intent)
        }
        binding.btnRemote.setOnClickListener {
            val profile = app.getProfile()
            if (profile.name.isBlank()) {
                Toast.makeText(this, R.string.need_to_setup_user, Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            val intent = Intent(this, WaitingAreaActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onStart() {
        super.onStart()
        val profile = app.getProfile()
        profile.icon?.let { binding.avatarIcon.setImageDrawable(it) }
    }
}