package pt.isec.multiplayerreversi.game.logic

enum class Piece(val char: Char) {
    Empty(' '),
    Dark('y'),
    Light('x'),
    Blue('z');
    //TODO 40 mostrar o caracter na peça

    companion object {
        fun getByChar(char: Char) = values().find { p -> p.char == char }
        fun getByOrdinal(ordinal: Int) = values().find { p -> p.ordinal == ordinal }
    }
}