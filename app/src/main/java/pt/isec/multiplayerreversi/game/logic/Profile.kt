package pt.isec.multiplayerreversi.game.logic

import android.graphics.drawable.Drawable

class Profile(
    var name: String = "",
    var icon: Drawable? = null,
) {
    // TODO add the photo somehow
    override fun toString(): String {
        return "Profile(name='$name', icon=$icon)"
    }
}