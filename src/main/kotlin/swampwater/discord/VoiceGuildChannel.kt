package swampwater.discord

data class VoiceGuildChannel(
        override val id: String,
        override val guildId: String?,
        override val name: String,
        override val position: Int,
        override val permissionOverwrites: List<Any>,
        val bitrate: Int?,
        val userLimit: Int?
) : GuildChannel