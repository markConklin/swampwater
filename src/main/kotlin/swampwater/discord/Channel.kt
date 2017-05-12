package swampwater.discord

data class Channel(
        val id: String,
        val guildId: String?,
        val name: String,
        val type: Channel.Type,
        val position: Int,
        val isPrivate: Boolean,
        val permissionOverwrites: List<Any>,
        val topic: String?,
        val lastMessageId: String?,
        val bitrate: Int?,
        val userLimit: Int?
) {
    enum class Type {
        text, private, voice, group
    }
}
