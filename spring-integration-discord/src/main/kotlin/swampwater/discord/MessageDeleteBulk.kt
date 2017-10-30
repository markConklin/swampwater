package swampwater.discord

data class MessageDeleteBulk(
        val ids: List<String>,
        val channelId: String
)
