package swampwater.discord

data class DMChannel(
        val id: String,
        val isPrivate: Boolean,
        val recipient: User,
        val lastMessageId: String
)

