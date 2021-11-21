package pt.isec.multiplayerreversi

import android.app.Application
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import pt.isec.multiplayerreversi.game.interactors.GamePlayer
import pt.isec.multiplayerreversi.game.logic.Game
import pt.isec.multiplayerreversi.game.logic.Profile
import java.io.File


class App : Application() {

    @Synchronized
    fun getProfile(): Profile {
        if (profile != null)
            return this.profile!!

        val pref = getSharedPreferences("user", MODE_PRIVATE)
        val name = pref.getString("name", "")
        var icon: Drawable? = null
        if (avatarFile.exists()) {
            openFileInput(avatarFileName).use {
                icon = BitmapDrawable(resources, it)
            }
        }
        val p = Profile(name!!, icon)
        profile = p
        return p
    }

    @Synchronized
    fun saveProfile(p: Profile) {
        val pref = getSharedPreferences("user", MODE_PRIVATE)
        val editor = pref.edit()
        editor.putString("name", p.name)
        editor.apply()
        profile = p
    }

    var game: Game? = null
    var proxy: GamePlayer? = null
    private var profile: Profile? = null

    companion object {
        const val OURTAG = "reversiTag"
        const val listeningPort = 43338
        val avatarFileName: String
            get() = "avatar.jpg"
    }

    private val avatarFile: File
        get() = File(filesDir, avatarFileName)
}
