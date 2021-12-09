package pt.isec.multiplayerreversi.game.interactors

class JsonTypes {

    class Setup {
        companion object {
            val SETTINGS = "settings"
            val READY = "ready"
            val HOST_EXITING = "exiting"
            val DATA = "data"
            val NEW_PLAYER = "new_player"
            val PLAYER_LEFT_WAITING_ROOM = "left_player"
            val STARTING = "starting"
            val PLAYERS = "players"
            val PLAYER_IDS = "ids"
            val SEND_PROFILE = "profile"
        }
    }

    class InGame {
        companion object {
            val GAME_TERMINATED = "game_terminated"
            val PLAYER_PASSED_TURN = "player_passed"
            val PLAYER_LEFT_RUNNING_GAME = "player_left"
            val PLAYER_DEVICE_READY = "player_ready"
            val BOARD_CHANGED = "board_changed"
            val PLAYER_CHANGED = "player_changed"
            val POSSIBLE_MOVES = "possible_moves"
            val NORMAL_PLAY = "normal_play"
            val BOMB_PLAY = "bomb_play"
            val TRADE_PLAY = "trade_play"
            val PLAYER_USED_NORMAL = "player_used_normal"
            val PLAYER_USED_BOMB = "player_used_bomb"
            val PLAYER_USED_TRADE = "player_used_trade"
            val GAME_FINISHED = "finished"
        }
    }
}