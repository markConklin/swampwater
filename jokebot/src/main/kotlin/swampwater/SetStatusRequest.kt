package swampwater

import swampwater.discord.Game
import swampwater.discord.Status
import java.time.Instant

data class SetStatusRequest(private val status: Status, val game: Game?) {
    var idle: Long? = null
        get() = if (status == Status.Idle) Instant.now().toEpochMilli() else null

}