package swampwater

import swampwater.discord.Embed


data class CreateMessage(
        val content: String,
        val nonce: String? = null,
        val tts: Boolean = false,
        val file: List<Byte>? = null,
        val embed: Embed? = null
)