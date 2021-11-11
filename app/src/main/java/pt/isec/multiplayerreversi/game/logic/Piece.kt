package pt.isec.multiplayerreversi.game.logic

enum class Piece(val char: Char, val id: Int) {
    Empty(' ', 0),
    Dark('y', 1),
    Light('x', 2),
    Blue('z', 3);
    //TODO 40 mostrar o caracter na peça

    companion object {
        fun getById(id: Int) = values().find { p -> p.id == id }
    }
}