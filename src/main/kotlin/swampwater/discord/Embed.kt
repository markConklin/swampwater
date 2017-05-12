package swampwater.discord

import java.net.URL
import java.time.LocalDateTime

data class Embed(
        val title: String,
        val type: String,
        val description: String,
        val url: URL,
        val timestamp: LocalDateTime,
        val color: Int,
        val footer: Footer,
        val image: Image,
        val thumbnail: Thumbnail,
        val video: Video,
        val provider: Provider,
        val author: Author,
        val fields: List<Field>
) {
    data class Footer(val text: String, val iconUrl: URL, val proxyIconURL: URL)
    data class Image(val url: URL, val proxyURL: URL, val height: Int, val width: Int)
    data class Thumbnail(val url: URL, val proxyURL: URL, val height: Int, val width: Int)
    data class Video(val url: URL, val height: Int, val width: Int)
    data class Provider(val name: String, val url: URL)
    data class Author(val name: String, val url: URL, val iconURL: URL, val proxyIconURL: URL)
    data class Field(val name: String, val value: String, val inline: Boolean)
}