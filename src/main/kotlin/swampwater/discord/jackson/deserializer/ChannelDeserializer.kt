package swampwater.discord.jackson.deserializer

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import swampwater.discord.Channel
import swampwater.discord.DMChannel
import swampwater.discord.GuildChannel


object ChannelDeserializer : StdDeserializer<Channel>(Channel::class.java) {
    override fun deserialize(parser: JsonParser, context: DeserializationContext): Channel = parser.readValueAsTree<JsonNode>().let {
        it.traverse(parser.codec).readValueAs(
                if (it["is_private"].asBoolean())
                    DMChannel::class.java
                else
                    GuildChannel::class.java
        )
    }
}