package pt.isec.multiplayerreversi.gamelogic

class Point(var x: Int, var y: Int) {

    fun add(point: Point) {
        x += point.x
        y += point.y
    }

    fun sub(point: Point) {
        x -= point.x
        y -= point.y
    }

}