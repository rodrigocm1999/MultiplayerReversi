package pt.isec.multiplayerreversi.activities

import android.content.Intent
import android.os.Bundle
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import pt.isec.multiplayerreversi.App
import pt.isec.multiplayerreversi.R
import pt.isec.multiplayerreversi.activities.others.WaitingPlayerListAdapter
import pt.isec.multiplayerreversi.game.interactors.networking.GamePlayerRemoteSide
import java.net.Socket
import kotlin.concurrent.thread

class WaitingAreaRemoteActivity : AppCompatActivity() {

    private var setupRemoteSide: GamePlayerRemoteSide? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_waiting_area_remote)

        title = getString(R.string.waiting_for_host)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val app = application as App
        val socket = app.temp as Socket
        val lvPlayers = findViewById<ListView>(R.id.lvPlayers)

        val adapter = WaitingPlayerListAdapter(this)
        thread {
            setupRemoteSide = GamePlayerRemoteSide(socket, app.getProfile(),
                arrivedPlayerCallback = { p -> // player is already inside list, which is the same on the list adapter
                    runOnUiThread { adapter.notifyDataSetChanged() }
                },
                leftPlayerCallback = {
                    runOnUiThread { adapter.notifyDataSetChanged() }
                },
                hostExitedCallback = {
                    runOnUiThread {
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
                adapter.players = setupRemoteSide?.getPlayers()!!
                adapter.notifyDataSetChanged()
            }
        }
        lvPlayers.adapter = adapter
    }

    override fun onSupportNavigateUp(): Boolean {
        super.onSupportNavigateUp()
        finish()
        setupRemoteSide?.leaveWaitingArea()
        return true
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
        setupRemoteSide?.leaveWaitingArea()
    }

}