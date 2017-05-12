package swampwater.discord

import java.time.OffsetDateTime

data class Member(
        val user: User,
        val nick: String?,
        val roles: List<String>,
        val joinedAt: OffsetDateTime?,
        val deaf: Boolean,
        val mute: Boolean
)
