package pt.isec.multiplayerreversi.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import pt.isec.multiplayerreversi.App
import pt.isec.multiplayerreversi.App.Companion.OURTAG
import pt.isec.multiplayerreversi.App.Companion.listeningPort
import pt.isec.multiplayerreversi.activities.others.PlayerListAdapter
import pt.isec.multiplayerreversi.R
import pt.isec.multiplayerreversi.databinding.ActivityWaitingAreaBinding
import pt.isec.multiplayerreversi.game.interactors.new_version.ConnectionsWelcomer
import pt.isec.multiplayerreversi.game.interactors.LocalOnline
import pt.isec.multiplayerreversi.game.logic.Game
import pt.isec.multiplayerreversi.game.logic.Piece
import pt.isec.multiplayerreversi.game.logic.Player
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketTimeoutException
import kotlin.concurrent.thread

class WaitingAreaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWaitingAreaBinding
    private lateinit var players: ArrayList<Player>
    private lateinit var connectionsWelcomer: ConnectionsWelcomer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWaitingAreaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        title = getString(R.string.waiting_area)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val app = application as App

        players = ArrayList(3)
        players.add(Player(app.getProfile(), Piece.Light))

        val adapter = PlayerListAdapter(this, players)
        binding.lvPlayers.adapter = adapter

        connectionsWelcomer = ConnectionsWelcomer(players) {
            runOnUiThread {
                adapter.notifyDataSetChanged()
                binding.btnStartGame.isEnabled = true
            }
        }

        binding.btnJoinGame.setOnClickListener {
            val editText = EditText(this).apply {
                this.isSingleLine = true
            }
            val dialog = AlertDialog.Builder(this)
                .setCancelable(true)
                .setTitle(R.string.enter_address)
                .setNegativeButton(R.string.cancel) { d, _ -> d.dismiss() }
                .setPositiveButton(R.string.join) { _, _ ->
                    thread {
                        val address = InetSocketAddress(editText.text.toString(), listeningPort)
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

        //TODO 20 verificar o exit do jogo, não deixar sair sem comfirmar
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    fun startGame() {
        val app = application as App
        val game = Game(8, players)
        app.game = game
        app.gamePlayer = LocalOnline(game, players[0])
        connectionsWelcomer.sendStart(game)
        val intent = Intent(this, GameActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        connectionsWelcomer.close()
    }

}