package swampwater.discord


data class CreateMessage(
        val content: String,
        val nonce: String? = null,
        val tts: Boolean = false,
        val file: List<Byte>? = null,
        val embed: Embed? = null
)