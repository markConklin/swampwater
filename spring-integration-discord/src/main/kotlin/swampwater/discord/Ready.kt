package swampwater.discord

data class Ready(
        val v: Int,
        val user: User,
        val privateChannels: List<DMChannel>,
        val guilds: List<UnavailableGuild>,
        val sessionId: String,
        val __trace: List<String>
)
