package pt.isec.multiplayerreversi.game.gamelogic

class Vector(var x: Int, var y: Int) {

    fun add(vector: Vector) {
        x += vector.x
        y += vector.y
    }

    fun sub(vector: Vector) {
        x -= vector.x
        y -= vector.y
    }

}