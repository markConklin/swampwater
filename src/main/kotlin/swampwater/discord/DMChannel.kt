package swampwater.discord

data class DMChannel(
        override val id: String,
        val recipients: List<User>,
        val lastMessageId: String
) : Channel
