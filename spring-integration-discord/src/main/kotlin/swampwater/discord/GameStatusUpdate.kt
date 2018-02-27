package swampwater.discord

data class GameStatusUpdate(val since: Long? = null, val game: Game? = null, val status: Status, val afk: Boolean)
