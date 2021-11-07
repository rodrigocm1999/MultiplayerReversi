package pt.isec.multiplayerreversi

import android.app.Application
import pt.isec.multiplayerreversi.game.interactors.senders.InteractionSenderProxy
import pt.isec.multiplayerreversi.game.logic.Game

val TAG = "reversi"


class App : Application() {

    var game: Game? = null
    var interactionSender: InteractionSenderProxy? = null
}