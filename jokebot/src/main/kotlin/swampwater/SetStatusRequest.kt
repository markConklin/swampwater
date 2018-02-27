package swampwater

import swampwater.discord.Game
import swampwater.discord.Status
import java.time.Instant

data class SetStatusRequest(val status: Status, val game: Game?, val afk: Boolean = false) {
    var idle: Long? = null
        get() = if (status == Status.idle) Instant.now().toEpochMilli() else null

}