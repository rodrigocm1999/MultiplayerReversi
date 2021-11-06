package pt.isec.multiplayerreversi.game.interactors

import java.io.Serializable

interface InteractionProxy : Serializable {

    fun playAt(line: Int, column: Int)

}