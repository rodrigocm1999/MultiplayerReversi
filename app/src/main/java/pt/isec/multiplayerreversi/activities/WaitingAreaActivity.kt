package pt.isec.multiplayerreversi.activities

import android.os.Bundle
import android.widget.Button
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import pt.isec.multiplayerreversi.App
import pt.isec.multiplayerreversi.R
import pt.isec.multiplayerreversi.databinding.ActivityWaitingAreaBinding
import pt.isec.multiplayerreversi.game.interactors.senders.LocalOnline
import pt.isec.multiplayerreversi.game.logic.Game
import pt.isec.multiplayerreversi.game.logic.Piece
import pt.isec.multiplayerreversi.game.logic.Player
import pt.isec.multiplayerreversi.game.logic.Profile

class WaitingAreaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWaitingAreaBinding
    private lateinit var playersListView: ListView
    private lateinit var players: ArrayList<Player>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityWaitingAreaBinding.inflate(layoutInflater)
        setContentView(binding.root)


        players = ArrayList(3)
        players.add(Player(Piece.Light, Profile("Futuro nome do player")))
        //TODO 20 por o nome do player


        //TODO 5 mostrar os players lista

        binding.btnJoinGame.setOnClickListener {
            //TODO 4 abrir o popup
            //TODO 5 juntar a um jogo
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