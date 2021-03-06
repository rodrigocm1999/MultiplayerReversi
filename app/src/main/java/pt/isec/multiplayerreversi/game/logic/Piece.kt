package pt.isec.multiplayerreversi.game.logic

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.appcompat.content.res.AppCompatResources
import pt.isec.multiplayerreversi.R

enum class Piece(val char: Char, private val pieceId: Int?, private val possiblePieceId: Int?) {
    Empty(' ', null, null),


    Light('x', R.drawable.piece_light, R.drawable.piece_possible_white),
    Dark('y', R.drawable.piece_dark, R.drawable.piece_possible_black),
    Blue('z', R.drawable.piece_blue, R.drawable.piece_possible_blue);
    // do not trust the IDE, this value is used but never directly, and IDE thinks it is very smart

    companion object {
        private val vals = arrayOf(Light, Dark, Blue)

        fun getByChar(char: Char) = values().find { p -> p.char == char }
        fun getByOrdinal(ordinal: Int) = values().find { p -> p.ordinal == ordinal }
        fun getPieces() = vals
    }


    private var drawable: Drawable? = null
    private var possibleDrawable: Drawable? = null

    fun getDrawable(context: Context): Drawable? {
        if (pieceId == null) return null
        if (drawable == null)
            drawable = AppCompatResources.getDrawable(context, pieceId)
        return drawable
    }

    fun getIsolatedDrawable(context: Context): Drawable? {
        if (pieceId == null) return null
        return AppCompatResources.getDrawable(context, pieceId)
    }

    fun getPossibleDrawable(context: Context): Drawable? {
        if (possiblePieceId == null) return null
        if (possibleDrawable == null)
            possibleDrawable = AppCompatResources.getDrawable(context, possiblePieceId)
        return possibleDrawable
    }
}