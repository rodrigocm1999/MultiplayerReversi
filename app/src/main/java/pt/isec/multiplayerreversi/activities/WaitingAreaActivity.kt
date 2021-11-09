package pt.isec.multiplayerreversi.activities

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import pt.isec.multiplayerreversi.App
import pt.isec.multiplayerreversi.R
import pt.isec.multiplayerreversi.databinding.ActivityWaitingAreaBinding
import pt.isec.multiplayerreversi.game.interactors.local.ConnectionsWelcomer
import pt.isec.multiplayerreversi.game.interactors.local.LocalOnline
import pt.isec.multiplayerreversi.game.logic.Game
import pt.isec.multiplayerreversi.game.logic.Piece
import pt.isec.multiplayerreversi.game.logic.Player
import pt.isec.multiplayerreversi.game.logic.Profile

class WaitingAreaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWaitingAreaBinding
    private lateinit var players: ArrayList<Player>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityWaitingAreaBinding.inflate(layoutInflater)
        setContentView(binding.root)


        players = ArrayList(3)
        players.add(Player(Piece.Light, Profile("Futuro nome do player")))
        //TODO 20 por o nome do player, ainda temos de ver onde guardar o player


        //TODO 5 mostrar os players lista
        val adapter =
            object : ArrayAdapter<Player>(this, R.layout.row_waiting_player) {
                override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val view: View = convertView
                        ?: layoutInflater.inflate(R.layout.row_waiting_player, parent, false)

                    val player = players[position]

                    val playerNameView =
                        view.findViewById<TextView>(R.id.textViewPlayerName)
                    playerNameView.text = player.getProfile().name

                    val playerPiece = player.getPiece()
                    val playerPieceView = view.findViewById<ImageView>(R.id.imgViewPlayerPiece)
                    playerPieceView.background = when (playerPiece) {
                        Piece.Dark -> AppCompatResources.getDrawable(context, R.drawable.piece_dark)
                        Piece.Light ->
                            AppCompatResources.getDrawable(context, R.drawable.piece_light)
                        Piece.Blue -> AppCompatResources.getDrawable(context, R.drawable.piece_blue)
                        else -> null
                    }
                    //TODO 15 meter o icone do utilizador

                    return view
                }

                override fun getCount() = players.size
            }
        binding.playersListView.adapter = adapter

        val app = application as App
        if (app.connectionsWelcomer == null)
            app.connectionsWelcomer = ConnectionsWelcomer {
                players.add(it.getOwnPlayer())
                adapter.notifyDataSetChanged()
            }
        //TODO 15 isto tem de parar de pegar ligações durante a execução de um jogo

        binding.btnJoinGame.setOnClickListener {
            //TODO 5 juntar a um jogo
            val editText = EditText(this)
            val dialog = AlertDialog.Builder(this)
                .setCancelable(true)
                .setTitle(R.string.enter_address)
                .setNegativeButton(R.string.cancel) { d, _ -> d.dismiss() }
                .setPositiveButton(R.string.join) { _, _ ->
                    Toast.makeText(this, "TODO", Toast.LENGTH_SHORT).show()
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
}