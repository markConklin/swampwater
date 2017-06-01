package swampwater.discord

data class GuildMemberUpdate(
        val guildId: String,
        val roles: List<String>,
        val user: User,
        val nick: String
)

