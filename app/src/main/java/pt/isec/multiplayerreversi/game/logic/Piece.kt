package pt.isec.multiplayerreversi.game.logic

enum class Piece(private val char: Char) {
    Empty(' '),
    Dark('y'),
    Light('x'),
    Blue('z');

    fun getChar() = char
}