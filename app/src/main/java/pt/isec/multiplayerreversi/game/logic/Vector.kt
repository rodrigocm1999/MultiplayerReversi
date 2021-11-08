package pt.isec.multiplayerreversi.game.logic

class Vector(var x: Int, var y: Int) {

    fun add(vector: Vector) {
        x += vector.x
        y += vector.y
    }

    fun sub(vector: Vector) {
        x -= vector.x
        y -= vector.y
    }

    override fun toString(): String {
        return "Vector(x=$x, y=$y)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Vector
        if (x != other.x) return false
        if (y != other.y) return false
        return true
    }

    override fun hashCode(): Int {
        var result = x
        result = 31 * result + y
        return result
    }
}