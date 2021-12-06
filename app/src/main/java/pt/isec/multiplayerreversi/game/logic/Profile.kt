package pt.isec.multiplayerreversi.game.logic

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.appcompat.content.res.AppCompatResources
import pt.isec.multiplayerreversi.R

class Profile(
    var name: String = "",
    var icon: BitmapDrawable? = null,
) {
    override fun toString(): String {
        return "Profile(name='$name', icon=$icon)"
    }

    fun getIcon(context: Context): Drawable {
        return icon ?: AppCompatResources.getDrawable(context, R.drawable.avatar_icon)!!
    }
}