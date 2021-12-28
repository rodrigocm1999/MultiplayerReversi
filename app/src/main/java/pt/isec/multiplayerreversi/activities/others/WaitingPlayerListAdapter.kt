package pt.isec.multiplayerreversi.activities.others

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import pt.isec.multiplayerreversi.R
import pt.isec.multiplayerreversi.game.logic.Player

class WaitingPlayerListAdapter(
    private val context: Context,
    var players: List<Player> = ArrayList(),
) : BaseAdapter(){

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View = convertView
            ?: LayoutInflater.from(context).inflate(R.layout.row_waiting_player, parent, false)

        val player = getItem(position)

        view.findViewById<TextView>(R.id.textViewPlayerName).apply {
            this.text = player.profile.name
        }
        view.findViewById<ImageView>(R.id.imgViewPlayerPiece).apply {
            this.setImageDrawable(player.piece.getDrawable(context))
        }
        view.findViewById<ImageView>(R.id.imgViewPlayerIcon).apply {
            this.setImageDrawable(player.profile.getIcon(context))
        }
        return view
    }


    override fun getCount() = players.size
    override fun getItem(pos: Int) = players[pos]
    override fun getItemId(pos: Int): Long = pos.toLong()
}