package swampwater.discord.jackson

import com.fasterxml.jackson.databind.module.SimpleModule
import swampwater.discord.Channel
import swampwater.discord.Dispatch
import swampwater.discord.GuildChannel
import swampwater.discord.jackson.deserializer.ChannelDeserializer
import swampwater.discord.jackson.deserializer.DispatchDeserializer
import swampwater.discord.jackson.deserializer.GuildChannelTypeDeserializer
import swampwater.discord.jackson.mixin.DispatchMixin

class DiscordJacksonModule : SimpleModule() {
    init {
        addDeserializer(Dispatch::class.java, DispatchDeserializer)
        addDeserializer(Channel::class.java, ChannelDeserializer)
        addDeserializer(GuildChannel.Type::class.java, GuildChannelTypeDeserializer)
        setMixInAnnotation(Dispatch::class.java, DispatchMixin::class.java)
    }
}