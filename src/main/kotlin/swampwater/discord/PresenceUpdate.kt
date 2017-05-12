package swampwater.discord


data class PresenceUpdate(
        val user: User,
        val roles: List<String>,
        val game: Game?,
        val guildId: String?,
        val status: String
)