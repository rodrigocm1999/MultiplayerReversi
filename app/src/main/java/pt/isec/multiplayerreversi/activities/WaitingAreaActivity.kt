package pt.isec.multiplayerreversi.activities

import android.os.Bundle
import android.widget.Button
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import pt.isec.multiplayerreversi.App
import pt.isec.multiplayerreversi.R
import pt.isec.multiplayerreversi.game.interactors.senders.LocalOnlineSender
import pt.isec.multiplayerreversi.game.logic.Game
import pt.isec.multiplayerreversi.game.logic.Piece
import pt.isec.multiplayerreversi.game.logic.Player
import pt.isec.multiplayerreversi.game.logic.Profile

class WaitingAreaActivity : AppCompatActivity() {

    private lateinit var playersListView: ListView
    private lateinit var players: ArrayList<Player>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_waiting_area)

        val playersAmount = intent.getIntExtra("playerAmount", -1)
        if (playersAmount < 0) throw IllegalStateException("Invalid playerAmount to wait for")

        playersListView = findViewById(R.id.playersListView)

        players = ArrayList(playersAmount)
        players.add(Player(Piece.Light, Profile("Futuro nome do player")))
        //TODO 20 por o nome do player


        //TODO 5 mostrar os players lista

        val joinGameBtn = findViewById<Button>(R.id.btnJoinGame)
        joinGameBtn.setOnClickListener {
            //TODO 4 abrir o popup
            //TODO 5 juntar a um jogo
        }


        //TODO 0 perguntar acerca da gestão do histório de resultados
        //TODO 20 verificar o exit do jogo, não deixar sair sem comfirmar
    }

    fun startGame() {
        val app = application as App
        val game = Game(8, players, players.random())
        app.game = game
        app.interactionSender = LocalOnlineSender(game, players[0])
    }
}