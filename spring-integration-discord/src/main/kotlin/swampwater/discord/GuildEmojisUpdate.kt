package swampwater.discord

data class GuildEmojisUpdate(
        val guildId: String,
        val emojis: List<Emoji>
)
