package swampwater.discord

data class DMChannel(
        val id: String,
        val recipient: User,
        val lastMessageId: String,
        val isPrivate: Boolean = true
) : Channel
