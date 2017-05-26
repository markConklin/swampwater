package swampwater.discord


interface GuildChannel : Channel{
    val guildId: String?
    val name: String
    val position: Int
    val permissionOverwrites: List<Any>
}