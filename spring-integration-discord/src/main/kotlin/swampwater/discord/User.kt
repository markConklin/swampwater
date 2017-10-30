package swampwater.discord

data class User(
        val id: String,
        val username: String?,
        val discriminator: String?,
        val avatar: String?,
        val bot: Boolean?,
        val mfaEnabled: Boolean?,
        val verified: Boolean?,
        val email: String?
)
