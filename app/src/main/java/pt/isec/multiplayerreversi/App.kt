package pt.isec.multiplayerreversi

import android.app.Application
import pt.isec.multiplayerreversi.game.interactors.InteractionProxy
import pt.isec.multiplayerreversi.game.logic.Game

const val OURTAG = "reversiTag"
const val listeningPort = 43338

//To connect to emulator, from remote phone
//telnet localhost 5554
//redir add tcp:43338:43338

class App : Application() {
    var game: Game? = null
    var interaction: InteractionProxy? = null
}