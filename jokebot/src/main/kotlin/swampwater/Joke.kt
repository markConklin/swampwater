package swampwater

data class Joke(private val setup: String, private val punchline: String) {
    fun toList() = listOf(setup, punchline)
}
