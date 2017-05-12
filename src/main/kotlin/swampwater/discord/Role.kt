package swampwater.discord

data class Role(
        val id: String,
        val name: String,
        val color: Int,
        val hoist: Boolean,
        val position: Int,
        val permissions: Int,
        val managed: Boolean,
        val mentionable: Boolean
)
