package swampwater.discord

data class VoiceState(
        val guildId: String,
        val channelId: String,
        val userId: String,
        val sessionId: String,
        val deaf: Boolean,
        val mute: Boolean,
        val selfDeaf: Boolean,
        val selfMute: Boolean,
        val suppress: Boolean
)
