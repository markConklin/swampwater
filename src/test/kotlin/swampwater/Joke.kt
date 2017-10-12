package swampwater

data class Joke(val setup: String, val punchline: String) {
    fun toList() = listOf(setup, punchline)
}
