package pt.isec.multiplayerreversi.game.interactors

class JsonTypes {

    class SetupTypes {
        companion object {
            val READY = "ready"
            val EXITING = "exiting"
            val DATA = "data"
            val NEW_PLAYER = "new_player"
            val STARTING = "starting"
            val PLAYERS = "players"
            val PLAYER_IDS = "ids"
            val SEND_PROFILE = "profile"
            val BOARD = "board"
        }
    }

    class InGameTypes {
        companion object {
            val GAME_STARTED = "started"
            val BOARD_CHANGED = "board_changed"
            val NORMAL_PLAY = "normal_play"
            val BOMB_PLAY = "bomb_play"
            val TRADE_PLAY = "trade_play"
            val GAME_FINISHED = "finished"
        }
    }
}