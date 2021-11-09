package pt.isec.multiplayerreversi

import android.app.Application
import pt.isec.multiplayerreversi.game.interactors.senders.InteractionProxy
import pt.isec.multiplayerreversi.game.logic.Game

val TAG = "reversiTag"
val listeningPort = 43578


class App : Application() {

    var game: Game? = null
    var interaction: InteractionProxy? = null
}