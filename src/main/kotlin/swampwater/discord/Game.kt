package swampwater.discord

import java.net.URL


data class Game(
        val name: String,
        val type: Type,
        val url: URL?
) {
    enum class Type {
        game,
        streaming
    }
}