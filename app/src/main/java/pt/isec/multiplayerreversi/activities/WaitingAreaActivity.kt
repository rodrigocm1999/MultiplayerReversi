package pt.isec.multiplayerreversi.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import pt.isec.multiplayerreversi.App
import pt.isec.multiplayerreversi.App.Companion.OURTAG
import pt.isec.multiplayerreversi.App.Companion.LISTENING_PORT
import pt.isec.multiplayerreversi.activities.others.WaitingPlayerListAdapter
import pt.isec.multiplayerreversi.R
import pt.isec.multiplayerreversi.databinding.ActivityWaitingAreaBinding
import pt.isec.multiplayerreversi.game.interactors.networking.ConnectionsWelcomer
import pt.isec.multiplayerreversi.game.interactors.LocalOnline
import pt.isec.multiplayerreversi.game.logic.Game
import pt.isec.multiplayerreversi.game.logic.Piece
import pt.isec.multiplayerreversi.game.logic.Player
import java.net.InetSocketAddress
import java.net.Socket
import kotlin.concurrent.thread
import android.net.wifi.WifiManager
import android.text.format.Formatter
import android.widget.BaseAdapter
import pt.isec.multiplayerreversi.game.logic.GameData


class WaitingAreaActivity : AppCompatActivity() {

    private lateinit var app: App
    private lateinit var binding: ActivityWaitingAreaBinding
    private var adapter: BaseAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWaitingAreaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        title = getString(R.string.waiting_area)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        app = application as App

        if (app.connectionsWelcomer == null) {
            val players = ArrayList<Player>(Game.PLAYER_LIMIT)
            players.add(Player(app.getProfile(), Piece.Light))
            app.connectionsWelcomer = ConnectionsWelcomer(app, players)
            Log.i(OURTAG,"Created ConnectionsWelcomer")
        }

        val connWelcom = app.connectionsWelcomer!!
        adapter = WaitingPlayerListAdapter(this, connWelcom.getPlayers())
        binding.lvPlayers.adapter = adapter
        connWelcom.playersChanged = { runOnUiThread { updateView() } }

        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val ip: String = Formatter.formatIpAddress(wifiManager.connectionInfo.ipAddress)
        binding.tvRoomAddress.text = ip

       setOnClickListeners()
    }

    private fun updateView() {
        adapter?.notifyDataSetChanged()
        val amount = app.connectionsWelcomer!!.getPlayers().size
        binding.tvPlayerAmount.text = amount.toString()
        binding.btnStartGame.isEnabled = amount >= 2
    }

    override fun finish() {
        super.finish()
        fullyCloseConnectionsWelcomer()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onStart() {
        super.onStart()
        updateView()
    }

    private fun fullyCloseConnectionsWelcomer() {
        app.connectionsWelcomer?.close()
        app.connectionsWelcomer = null
    }

    private fun startGame() {
        val players = app.connectionsWelcomer!!.getPlayers()
        val game = Game(GameData(app.sharedGamePreferences, players))
        app.game = game
        val thisPlayer = players[0]
        val gamePlayer = LocalOnline(game, players[0])
        thisPlayer.callbacks = gamePlayer
        app.gamePlayer = gamePlayer

        app.connectionsWelcomer?.sendStart(game)
        app.connectionsWelcomer?.close()
        app.connectionsWelcomer = null
        val intent = Intent(this, GameActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun setOnClickListeners() {
        binding.btnJoinGame.setOnClickListener {
            val editText = EditText(this).apply {
                this.isSingleLine = true
            }
            val dialog = AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle(R.string.enter_address)
                .setNegativeButton(R.string.cancel) { d, _ -> d.dismiss() }
                .setPositiveButton(R.string.join) { _, _ ->
                    thread {
                        val address = InetSocketAddress(editText.text.toString(), LISTENING_PORT)
                        val socket = Socket()
                        try {
                            socket.connect(address, 2000)
                            Log.i(OURTAG, "connected socket")
                            app.temp = socket
                            finish()
                            val intent = Intent(this, WaitingAreaRemoteActivity::class.java)
                            startActivity(intent)
                        } catch (e: Exception) {
                            runOnUiThread {
                                Toast.makeText(this,
                                    "${getString(R.string.failed_to_connect)} $address",
                                    Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }.setView(editText)
                .create()
            dialog.show()
        }

        binding.btnStartGame.setOnClickListener {
            startGame()
        }
    }
}