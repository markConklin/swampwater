package swampwater.discord.jackson.deserializer

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import swampwater.discord.Channel
import swampwater.discord.DMChannel
import swampwater.discord.GuildChannel


object ChannelDeserializer : StdDeserializer<Channel>(Channel::class.java) {
    override fun deserialize(parser: JsonParser, context: DeserializationContext): Channel {
        val root: JsonNode = parser.readValueAsTree()
        return if (root["is_private"].asBoolean())
            root.traverse(parser.codec).readValueAs(DMChannel::class.java)
        else
            root.traverse(parser.codec).readValueAs(GuildChannel::class.java)

    }
}