package pt.isec.multiplayerreversi.activities

import android.os.Bundle
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import pt.isec.multiplayerreversi.App
import pt.isec.multiplayerreversi.PlayerListAdapter
import pt.isec.multiplayerreversi.R
import pt.isec.multiplayerreversi.game.interactors.GamePlayer

class WaitingAreaRemoteActivity : AppCompatActivity() {

    val app = application as App

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_waiting_area_remote)

        title = getString(R.string.waiting_for_host)

        val app = application as App

        val proxy: GamePlayer = app.proxy!!

        val lvPlayers = findViewById<ListView>(R.id.playersListView)
        val adapter = PlayerListAdapter(this, proxy.getPlayers())
        lvPlayers.adapter = adapter
    }

    //TODO quando se dá join muda-se para esta janela

}