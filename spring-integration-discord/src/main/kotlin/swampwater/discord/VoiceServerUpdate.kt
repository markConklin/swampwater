package swampwater.discord

data class VoiceServerUpdate(
        val token: String,
        val guildId: String,
        val endpoint: String
)
