package pt.isec.multiplayerreversi.game.logic

import java.io.Serializable

enum class Piece(private val char: Char) : Serializable {
    Empty(' '),
    Dark('y'),
    Light('x'),
    Blue('z');

    fun getChar() = char
}