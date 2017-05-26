package swampwater.discord

import java.net.URL
import java.time.OffsetDateTime

data class MessageCreate(
        val id: String,
        val channelId: String,
        val author: User,
        val content: String,
        val timestamp: OffsetDateTime,
        val tts: Boolean,
        val mentionEveryone: Boolean,
        val mentions: List<User>,
        val mentionRoles: List<String>,
        val attachments: List<Attachment>,
        val embeds: List<Embed>,
        val reactions: List<Reaction>?,
        val nonce: String?,
        val pinned: Boolean,
        val webHook: String?
) {
    data class Attachment(
            val id: String,
            val filename: String,
            val size: Int,
            val url: URL,
            val proxyUrl: URL,
            val height: Int?,
            val width: Int?
    )
}

