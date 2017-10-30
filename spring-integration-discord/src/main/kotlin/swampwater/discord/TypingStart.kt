package swampwater.discord

import java.time.OffsetDateTime

data class TypingStart(
        val userId: String,
        val timestamp: OffsetDateTime,
        val channelId: String
)
