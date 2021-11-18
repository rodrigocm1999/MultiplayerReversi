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
import pt.isec.multiplayerreversi.PlayerListAdapter
import pt.isec.multiplayerreversi.R
import pt.isec.multiplayerreversi.databinding.ActivityWaitingAreaBinding
import pt.isec.multiplayerreversi.game.interactors.ConnectionsWelcomer
import pt.isec.multiplayerreversi.game.interactors.LocalOnline
import pt.isec.multiplayerreversi.game.interactors.LocalRemoteGameProxy
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

        val app = application as App

        players = ArrayList(3)
        players.add(Player(app.getProfile(), Piece.Light))

        val adapter = PlayerListAdapter(this, players)
        binding.playersListView.adapter = adapter

        connectionsWelcomer = ConnectionsWelcomer(players) {
            players.add(it.getOwnPlayer())
            adapter.notifyDataSetChanged()
            binding.btnStartGame.isEnabled = true
        }

        //TODO 20 eventualmente temos de fechar o socket depois de sair do jogo online

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
                            val proxy = LocalRemoteGameProxy(socket, app.getProfile())
                            app.proxy = proxy
//                            runOnUiThread {
//                                players = proxy.getPlayers()
//                                adapter.notifyDataSetChanged()}
                            finish()
                            val intent = Intent(this, WaitingAreaRemoteActivity::class.java)
                            startActivity(intent)
                        } catch (e: SocketTimeoutException) {
                            runOnUiThread {
                                Toast.makeText(this, R.string.failed_to_connect,
                                    Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }.setView(editText)
                .create()
            dialog.show()
        }

        //TODO 20 verificar o exit do jogo, não deixar sair sem comfirmar
        //TODO 100 alterar a fonte
        //TODO 80 alterar o icon
    }

    fun startGame() {
        val app = application as App
        val game = Game(8, players, players.random())
        app.game = game
        app.proxy = LocalOnline(game, players[0])
    }

    override fun onDestroy() {
        super.onDestroy()
        connectionsWelcomer.close()
    }

}