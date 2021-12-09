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

    private lateinit var app: App
    private var setupRemoteSide: GamePlayerRemoteSide? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_waiting_area_remote)

        title = getString(R.string.waiting_for_host)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        app = application as App
        val socket = app.temp as Socket

        val lvPlayers = findViewById<ListView>(R.id.lvPlayers)
        val adapter = WaitingPlayerListAdapter(this)
        lvPlayers.adapter = adapter


        if (app.setupRemoteSide == null) {
            thread {
                setupRemoteSide = GamePlayerRemoteSide(socket, app.getProfile(),
                    arrivedPlayerCallback = { runOnUiThread { adapter.notifyDataSetChanged() } },
                    leftPlayerCallback = { runOnUiThread { adapter.notifyDataSetChanged() } },
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
                app.setupRemoteSide = setupRemoteSide
            }
        } else {
            setupRemoteSide = app.setupRemoteSide
            runOnUiThread {
                adapter.players = setupRemoteSide?.getPlayers()!!
                adapter.notifyDataSetChanged()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        super.onSupportNavigateUp()
        setupRemoteSide?.leaveWaitingArea()
        finish()
        return true
    }

    //is called when we leave this area, but not on screen rotation
    override fun finish() {
        super.finish()
        app.setupRemoteSide = null
    }


    override fun onBackPressed() {
        super.onBackPressed()
        setupRemoteSide?.leaveWaitingArea()
        finish()
    }

}