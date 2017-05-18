package swampwater.discord.jackson

import com.fasterxml.jackson.databind.module.SimpleModule
import swampwater.discord.Channel
import swampwater.discord.Dispatch
import swampwater.discord.GuildChannel
import swampwater.discord.jackson.deserializer.ChannelDeserializer
import swampwater.discord.jackson.deserializer.DispatchDeserializer
import swampwater.discord.jackson.mixin.DispatchMixin
import swampwater.discord.jackson.mixin.GuildChannelMixin

class DiscordJacksonModule : SimpleModule() {
    init {
        addDeserializer(Dispatch::class.java, DispatchDeserializer)
        addDeserializer(Channel::class.java, ChannelDeserializer)
        setMixInAnnotation(Dispatch::class.java, DispatchMixin::class.java)
        setMixInAnnotation(GuildChannel::class.java, GuildChannelMixin::class.java)
    }
}