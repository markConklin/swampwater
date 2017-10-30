package swampwater.discord

data class TextGuildChannel(
        override val id: String,
        override val guildId: String?,
        override val name: String,
        override val position: Int,
        override val permissionOverwrites: List<Any>,
        val topic: String?,
        val lastMessageId: String?
) : GuildChannel