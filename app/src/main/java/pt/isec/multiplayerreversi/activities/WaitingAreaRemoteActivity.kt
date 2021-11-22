package pt.isec.multiplayerreversi.activities

import android.os.Bundle
import android.widget.ListView
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

        val lvPlayers = findViewById<ListView>(R.id.playersListView)

        val adapter = PlayerListAdapter(this)
        thread {
            val proxy = GameSetupRemoteSide(socket, app.getProfile()) {
                adapter.notifyDataSetChanged()
            }
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