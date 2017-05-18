package swampwater.discord

data class TextGuildChannel(
        var id: String,
        var guildId: String?,
        var name: String,
        var position: Int,
        var permissionOverwrites: List<Any>,
        var topic: String?,
        var lastMessageId: String?
) : GuildChannel