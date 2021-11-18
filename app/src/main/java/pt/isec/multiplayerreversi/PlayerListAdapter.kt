package pt.isec.multiplayerreversi

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import pt.isec.multiplayerreversi.game.logic.Piece
import pt.isec.multiplayerreversi.game.logic.Player

class PlayerListAdapter(
    private val context: Context,
    private val players: List<Player>,
) : BaseAdapter() {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View = convertView
            ?: LayoutInflater.from(context).inflate(R.layout.row_waiting_player, parent, false)
        val player = getItem(position)

        view.findViewById<TextView>(R.id.textViewPlayerName).apply {
            this.text = player.profile.name
        }
        view.findViewById<ImageView>(R.id.imgViewPlayerPiece).apply {
            val resource = when (player.piece) {
                Piece.Dark -> R.drawable.piece_dark
                Piece.Light -> R.drawable.piece_light
                Piece.Blue -> R.drawable.piece_blue
                else -> R.drawable.piece_dark
            }
            this.setImageResource(resource)
        }
        view.findViewById<ImageView>(R.id.imgViewPlayerIcon).apply {
            this.setImageDrawable(player.profile.icon)
        }
        return view
    }

    override fun getCount() = players.size
    override fun getItem(pos: Int) = players[pos]
    override fun getItemId(pos: Int): Long = pos.toLong()
}