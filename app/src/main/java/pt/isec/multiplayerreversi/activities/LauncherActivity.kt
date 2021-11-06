package pt.isec.multiplayerreversi.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import pt.isec.multiplayerreversi.R
import pt.isec.multiplayerreversi.databinding.ActivityLaucherBinding
import pt.isec.multiplayerreversi.game.interactors.Local1V1InteractionProxy
import pt.isec.multiplayerreversi.game.logic.Piece
import pt.isec.multiplayerreversi.game.logic.Player
import pt.isec.multiplayerreversi.game.logic.Profile

class LauncherActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLaucherBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLaucherBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val notYetImplementedToast = View.OnClickListener {
            Toast.makeText(this, R.string.notYetImplemented, Toast.LENGTH_SHORT).show()
        }
        binding.btnRemote1v1.setOnClickListener(notYetImplementedToast)
        binding.btnRemote1v1v1.setOnClickListener(notYetImplementedToast)
        binding.avatarIcon.setOnClickListener {
            val intent = Intent(this,EditProfileActivity::class.java)
            startActivity(intent)
        }

        binding.btnLocal1v1.setOnClickListener {
            val intent = Intent(this, GameActivity::class.java)

            intent.putExtra("boardSideLength", 8)

            val players = ArrayList<Player>(2)
            players.add(Player(Piece.Dark, Profile(resources.getString(R.string.dark_piece))))
            players.add(Player(Piece.Light, Profile(resources.getString(R.string.light_piece))))
            intent.putExtra("players", players)

            intent.putExtra("interactionProxy", Local1V1InteractionProxy()) // TODO 1 fix this

            startActivity(intent)
        }
        //TODO 100 mudar drawable para icon do utilizador
        //TODO 10000 fazer logo da pixa
    }

}