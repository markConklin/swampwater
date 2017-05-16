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
                    "GUILD_CREATE" to Guild::class.java,
                    "MESSAGE_CREATE" to Message::class.java,
                    "READY" to Ready::class.java,
                    "TYPING_START" to TypingStart::class.java,
                    "PRESENCE_UPDATE" to PresenceUpdate::class.java
            ),
            Op.Hello to mapOf(
                    null to Hello::class.java
            ),
            Op.InvalidSession to mapOf(
                    null to Boolean::class.java
            )
    )

    override fun deserialize(parser: JsonParser, context: DeserializationContext): Dispatch {
        val node: JsonNode = parser.readValueAsTree()
        val op = Op::class.java.enumConstants[node["op"].asInt()]
        val t = node["t"].asText(null)
        val d = node["d"]
        val event = if (d is NullNode) null else {
            val type = opMapping[op]?.get(t)
            type ?: context.reportMappingException("unrecognized op/type %s/%s", op, t)
            d.traverse(parser.codec).readValueAs(type)
        }
        return Dispatch(op, event, node["s"].asInt(), t)
    }
}