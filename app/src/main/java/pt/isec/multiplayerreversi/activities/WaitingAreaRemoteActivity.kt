package pt.isec.multiplayerreversi.activities

import android.content.Intent
import android.os.Bundle
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import pt.isec.multiplayerreversi.App
import pt.isec.multiplayerreversi.R
import pt.isec.multiplayerreversi.activities.others.PlayerListAdapter
import pt.isec.multiplayerreversi.game.interactors.new_version.GameSetupRemoteSide
import java.net.Socket
import kotlin.concurrent.thread

class WaitingAreaRemoteActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_waiting_area_remote)

        title = getString(R.string.waiting_for_host)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val app = application as App
        val socket = app.temp as Socket
        val lvPlayers = findViewById<ListView>(R.id.lvPlayers)

        val adapter = PlayerListAdapter(this)
        thread {
            val proxy = GameSetupRemoteSide(socket, app.getProfile(),
                arrivedNewPlayerCallback = { p -> // player is already inside list, which is the same on the list adapter
                    runOnUiThread { adapter.notifyDataSetChanged() }
                },
                hostExitedCallback = {
                    runOnUiThread { //TODO 50 maybe make this different, maybe
                        Toast.makeText(this, R.string.host_exited, Toast.LENGTH_LONG).show()
                        finish()
                    }
                },
                gameStartingCallback = { gamePlayer ->
                    app.gamePlayer = gamePlayer
                    finish()
                    val intent = Intent(this, GameActivity::class.java)
                    startActivity(intent)
                })
            runOnUiThread {
                adapter.players = proxy.getPlayers()
                adapter.notifyDataSetChanged()
            }
        }
        lvPlayers.adapter = adapter
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

}