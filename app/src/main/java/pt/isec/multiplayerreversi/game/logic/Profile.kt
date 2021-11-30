package pt.isec.multiplayerreversi.game.logic

import android.graphics.drawable.BitmapDrawable

class Profile(
    var name: String = "",
    var icon: BitmapDrawable? = null,
//    var imagePath: String? = null,
) {
    override fun toString(): String {
        return "Profile(name='$name', icon=$icon)"
    }
}