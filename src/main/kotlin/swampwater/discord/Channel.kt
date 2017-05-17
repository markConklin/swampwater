package swampwater.discord

sealed class Channel

data class DMChannel(
        val id: String,
        val recipient: User,
        val lastMessageId: String,
        val isPrivate: Boolean = true
) : Channel()

data class GuildChannel(
        val id: String,
        val guildId: String?,
        val name: String,
        val type: GuildChannel.Type,
        val position: Int,
        val permissionOverwrites: List<Any>,
        val topic: String?,
        val lastMessageId: String?,
        val bitrate: Int?,
        val userLimit: Int?,
        val isPrivate: Boolean = false
) : Channel() {
    enum class Type {
        text, voice
    }
}
