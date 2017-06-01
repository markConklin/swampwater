package swampwater.discord.jackson.deserializer

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.node.NullNode
import swampwater.discord.*

object DispatchDeserializer : StdDeserializer<Dispatch>(Dispatch::class.java) {
    private val mapping: Map<Op, Map<out String?, Class<out Any>>> = mapOf(
            Op.Dispatch to mapOf(
                    "CHANNEL_CREATE" to Channel::class.java,
                    "CHANNEL_DELETE" to Channel::class.java,
                    "CHANNEL_UPDATE" to GuildChannel::class.java,
                    "GUILD_BAN_ADD" to GuildBan::class.java,
                    "GUILD_BAN_REMOVE" to GuildBan::class.java,
                    "GUILD_CREATE" to Guild::class.java,
                    "GUILD_DELETE" to GuildDelete::class.java,
                    "GUILD_EMOJIS_UPDATE" to GuildEmojisUpdate::class.java,
                    "GUILD_INTEGRATIONS_UPDATE" to String::class.java,
                    "GUILD_MEMBER_ADD" to GuildMemberAdd::class.java,
                    "GUILD_MEMBER_CHUNK" to GuildMemberChunk::class.java,
                    "GUILD_MEMBER_REMOVE" to GuildMemberRemove::class.java,
                    "GUILD_MEMBER_UPDATE" to GuildMemberUpdate::class.java,
                    "GUILD_ROLE_CREATE" to GuildRole::class.java,
                    "GUILD_ROLE_DELETE" to GuildRoleDelete::class.java,
                    "GUILD_ROLE_UPDATE" to GuildRole::class.java,
                    "GUILD_UPDATE" to Guild::class.java,
                    "MESSAGE_CREATE" to Message::class.java,
                    "MESSAGE_DELETE" to MessageDelete::class.java,
                    "MESSAGE_DELETE_BULK" to MessageDeleteBulk::class.java,
                    "MESSAGE_UPDATE" to Message::class.java,
                    "PRESENCE_UPDATE" to PresenceUpdate::class.java,
                    "READY" to Ready::class.java,
                    "RESUMED" to Resumed::class.java,
                    "TYPING_START" to TypingStart::class.java,
                    "USER_UPDATE" to User::class.java,
                    "VOICE_STATE_UPDATE" to VoiceState::class.java,
                    "VOICE_SERVER_UPDATE" to VoiceServerUpdate::class.java
            ),
            Op.Hello to mapOf(
                    null to Hello::class.java
            ),
            Op.InvalidSession to mapOf(
                    null to Boolean::class.java
            ),
            Op.Reconnect to mapOf(
                    null to String::class.java
            )
    )

    override fun deserialize(parser: JsonParser, context: DeserializationContext): Dispatch {
        val root: JsonNode = parser.readValueAsTree()
        val op = root["op"].traverse(parser.codec).readValueAs(Op::class.java)
        val t = root["t"].asText(null)
        val event = root["d"].let {
            if (it is NullNode) {
                null
            } else {
                val type = mapping[op]?.get(t)
                type ?: context.reportMappingException("unrecognized op/type %s/%s", op, t)
                it.traverse(parser.codec).readValueAs(type)
            }
        }
        return Dispatch(op, event, root["s"].asInt(), t)
    }
}