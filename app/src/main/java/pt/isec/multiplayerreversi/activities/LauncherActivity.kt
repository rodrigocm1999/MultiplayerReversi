package pt.isec.multiplayerreversi.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import pt.isec.multiplayerreversi.App
import pt.isec.multiplayerreversi.R
import pt.isec.multiplayerreversi.databinding.ActivityLauncherBinding
import pt.isec.multiplayerreversi.game.interactors.Local1V1Play
import pt.isec.multiplayerreversi.game.logic.Game
import pt.isec.multiplayerreversi.game.logic.Piece
import pt.isec.multiplayerreversi.game.logic.Player
import pt.isec.multiplayerreversi.game.logic.Profile
import kotlin.concurrent.thread
import android.net.ConnectivityManager
import android.net.NetworkInfo







class LauncherActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLauncherBinding
    private lateinit var app: App

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLauncherBinding.inflate(layoutInflater)
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
            val p1 = Player(Profile(resources.getString(R.string.dark_piece)), Piece.Dark)
            players.add(p1)
            players.add(Player(Profile(resources.getString(R.string.light_piece)), Piece.Light))

            val game = Game(8, players)
            app.game = game
            val proxy = Local1V1Play(game)
            p1.callbacks = proxy
            app.gamePlayer = proxy
            startActivity(intent)
        }
        binding.btnRemote.setOnClickListener {
            val connManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
            val mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
            if (!mWifi!!.isConnected) {
                Toast.makeText(this, R.string.need_wifi, Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

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
        thread {
            val profile = app.getProfile()
            runOnUiThread {
                profile.icon?.let { binding.avatarIcon.setImageDrawable(it) }
            }
        }
    }
}