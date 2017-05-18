package swampwater

data class Joke(val setup: String, val punchline: String) {
    fun toList(): List<String> = listOf(setup, punchline)
}
