package swampwater.discord

data class VoiceGuildChannel(
        var id: String,
        var guildId: String?,
        var name: String,
        var position: Int,
        var permissionOverwrites: List<Any>,
        val bitrate: Int?,
        val userLimit: Int?
) : GuildChannel