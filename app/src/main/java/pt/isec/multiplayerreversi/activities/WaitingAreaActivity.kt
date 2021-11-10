package pt.isec.multiplayerreversi.activities

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import pt.isec.multiplayerreversi.App
import pt.isec.multiplayerreversi.R
import pt.isec.multiplayerreversi.databinding.ActivityWaitingAreaBinding
import pt.isec.multiplayerreversi.game.interactors.local.ConnectionsWelcomer
import pt.isec.multiplayerreversi.game.interactors.local.LocalOnline
import pt.isec.multiplayerreversi.game.interactors.remote.InteractionRemoteProxy
import pt.isec.multiplayerreversi.game.logic.Game
import pt.isec.multiplayerreversi.game.logic.Piece
import pt.isec.multiplayerreversi.game.logic.Player
import pt.isec.multiplayerreversi.game.logic.Profile
import pt.isec.multiplayerreversi.listeningPort
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketTimeoutException

class WaitingAreaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWaitingAreaBinding
    private lateinit var players: ArrayList<Player>
    private lateinit var connectionsWelcomer: ConnectionsWelcomer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityWaitingAreaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        players = ArrayList(3)
        players.add(Player(Profile("Futuro nome do player"), Piece.Light))
        //TODO 20 por o nome do player, ainda temos de ver onde guardar o player

        //TODO 5 mostrar os players lista
        val adapter =
            object : BaseAdapter() {
                override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val view: View = convertView
                        ?: layoutInflater.inflate(R.layout.row_waiting_player, parent, false)
                    val player = getItem(position)

                    view.findViewById<TextView>(R.id.textViewPlayerName).apply {
                        this.text = player.getProfile().name
                    }
                    view.findViewById<ImageView>(R.id.imgViewPlayerPiece).apply {
                        val resource = when (player.getPiece()) {
                            Piece.Dark -> R.drawable.piece_dark
                            Piece.Light -> R.drawable.piece_light
                            Piece.Blue -> R.drawable.piece_blue
                            else -> R.drawable.piece_dark
                        }
                        this.setImageResource(resource)
                    }
                    //TODO 16 meter o icone do utilizador
                    return view
                }

                override fun getCount() = players.size
                override fun getItem(pos: Int) = players[pos]
                override fun getItemId(pos: Int): Long = pos.toLong()
            }
        binding.playersListView.adapter = adapter

        connectionsWelcomer = ConnectionsWelcomer {
            players.add(it.getOwnPlayer())
            adapter.notifyDataSetChanged()
        }
        //TODO 15 isto tem de parar de pegar ligações durante a execução de um jogo
        //TODO 20 eventualmente temos de fechar o socket depois de sair do jogo online

        binding.btnJoinGame.setOnClickListener {
            //TODO 5 juntar a um jogo
            val editText = EditText(this)
            val dialog = AlertDialog.Builder(this)
                .setCancelable(true)
                .setTitle(R.string.enter_address)
                .setNegativeButton(R.string.cancel) { d, _ -> d.dismiss() }
                .setPositiveButton(R.string.join) { _, _ ->
                    val address = InetSocketAddress(editText.text.toString(), listeningPort)
                    val socket = Socket()
                    try {
                        socket.connect(address, 2000)
                    } catch (e: SocketTimeoutException) {
                        Toast.makeText(this, R.string.failed_to_connect, Toast.LENGTH_LONG).show()
                        return@setPositiveButton
                    }

                    //TODO 13 put the right player object
                    val profile = Profile("asd")
                    InteractionRemoteProxy(socket, profile)
                }.setView(editText)
                .create()
            dialog.show()
        }

        //TODO 0 perguntar acerca da gestão do histório de resultados
        //TODO 20 verificar o exit do jogo, não deixar sair sem comfirmar
        //TODO 100 alterar a fonte
        //TODO 80 alterar o icon
    }

    fun startGame() {
        val app = application as App
        val game = Game(8, players, players.random())
        app.game = game
        app.interaction = LocalOnline(game, players[0])
    }

    override fun onDestroy() {
        super.onDestroy()
        connectionsWelcomer.close()
    }

}