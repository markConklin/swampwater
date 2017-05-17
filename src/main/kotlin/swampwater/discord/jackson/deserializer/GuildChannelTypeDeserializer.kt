package swampwater.discord.jackson.deserializer

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import swampwater.discord.GuildChannel


object GuildChannelTypeDeserializer : StdDeserializer<GuildChannel.Type>(GuildChannel.Type::class.java) {
    override fun deserialize(parser: JsonParser, context: DeserializationContext): GuildChannel.Type {
        return if (parser.valueAsInt == 0) GuildChannel.Type.text
        else GuildChannel.Type.voice
    }
}