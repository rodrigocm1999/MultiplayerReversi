package pt.isec.multiplayerreversi.game.interactors

interface Consum<T> {
    fun accept(t: T)
}