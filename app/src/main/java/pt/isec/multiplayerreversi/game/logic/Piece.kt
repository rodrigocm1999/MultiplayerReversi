package pt.isec.multiplayerreversi.game.gamelogic

enum class Piece(private val char: Char) {
    Empty(' '),
    Dark('y'),
    Light('x'),
    Blue('z');

    fun getChar() = char
}