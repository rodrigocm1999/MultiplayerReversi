package pt.isec.multiplayerreversi.game.interactors.senders

import java.io.Serializable

interface InteractionSenderProxy : Serializable {

    fun playAt(line: Int, column: Int)

}