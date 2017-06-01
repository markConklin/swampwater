package swampwater.discord

data class GuildMemberChunk(
        val guildId: String,
        val members: List<Member>
)
