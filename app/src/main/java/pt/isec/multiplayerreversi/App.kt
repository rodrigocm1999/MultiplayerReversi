package pt.isec.multiplayerreversi

import android.app.Application
import pt.isec.multiplayerreversi.game.interactors.InteractionProxy
import pt.isec.multiplayerreversi.game.interactors.local.ConnectionsWelcomer
import pt.isec.multiplayerreversi.game.logic.Game

const val TAG = "reversiTag"
const val listeningPort = 43578


class App : Application() {

    var connectionsWelcomer: ConnectionsWelcomer? = null
    var game: Game? = null
    var interaction: InteractionProxy? = null
}