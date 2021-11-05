package pt.isec.multiplayerreversi.gamelogic

enum class Piece(private val char: Char) {
    Empty(' '),
    Dark('y'),
    Light('x'),
    Blue('z')
}