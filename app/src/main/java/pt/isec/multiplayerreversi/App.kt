package pt.isec.multiplayerreversi

import android.app.Application
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import pt.isec.multiplayerreversi.game.interactors.GamePlayer
import pt.isec.multiplayerreversi.game.logic.Game
import pt.isec.multiplayerreversi.game.logic.Profile
import java.io.File
import kotlin.concurrent.thread


class App : Application() {

    @Synchronized
    fun getProfile(): Profile {
        if (profile != null)
            return this.profile!!

        val pref = getSharedPreferences("user", MODE_PRIVATE)
        val name = pref.getString("name", "")
        var icon: BitmapDrawable? = null
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
    fun saveProfile(p: Profile, saveImage: Boolean) {
        val pref = getSharedPreferences("user", MODE_PRIVATE)
        val editor = pref.edit()
        editor.putString("name", p.name)
        editor.apply()
        profile = p
        if (saveImage) {
            thread {
                openFileOutput(avatarFileName, MODE_PRIVATE).use {
                    p.icon!!.bitmap.compress(Bitmap.CompressFormat.JPEG, 95, it)
                }
            }
        }
    }

    var game: Game? = null
    var gamePlayer: GamePlayer? = null

    private var profile: Profile? = null

    var temp: Any? = null
    var tempProfile: Profile? = null

    companion object {
        const val OURTAG = "reversiTag"
        const val listeningPort = 43338
        val avatarFileName: String
            get() = "avatar.jpg"
    }

    private val avatarFile: File
        get() = File(filesDir, avatarFileName)
}
