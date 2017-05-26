package swampwater.discord.jackson.deserializer

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.node.NullNode
import swampwater.discord.*

object DispatchDeserializer : StdDeserializer<Dispatch>(Dispatch::class.java) {
    private val opMapping: Map<Op, Map<out String?, Class<out Any>>> = mapOf(
            Op.Dispatch to mapOf(
                    "GUILD_CREATE" to GuildCreate::class.java,
                    "MESSAGE_CREATE" to MessageCreate::class.java,
                    "PRESENCE_UPDATE" to PresenceUpdate::class.java,
                    "READY" to Ready::class.java,
                    "TYPING_START" to TypingStart::class.java
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
        val op = Op::class.java.enumConstants[root["op"].asInt()]
        val t = root["t"].asText(null)
        val d = root["d"]
        val event = if (d is NullNode) null else {
            val type = opMapping[op]?.get(t)
            type ?: context.reportMappingException("unrecognized op/type %s/%s", op, t)
            d.traverse(parser.codec).readValueAs(type)
        }
        return Dispatch(op, event, root["s"].asInt(), t)
    }
}